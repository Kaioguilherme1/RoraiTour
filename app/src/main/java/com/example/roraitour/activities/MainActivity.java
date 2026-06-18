package com.example.roraitour.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.example.roraitour.R;
import com.example.roraitour.adapters.TouristPlaceAdapter;
import com.example.roraitour.databinding.ActivityMainBinding;
import com.example.roraitour.models.CustomPlace;
import com.example.roraitour.models.TouristPlace;
import com.example.roraitour.repositories.CustomPlaceRepository;
import com.example.roraitour.repositories.OpenTripMapRepository;
import com.example.roraitour.utils.Constants;
import com.example.roraitour.utils.NetworkUtils;
import com.example.roraitour.utils.ImageLoader;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;
    private TouristPlaceAdapter adapter;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private OpenTripMapRepository openTripMapRepository;
    private CustomPlaceRepository customPlaceRepository;
    private Location lastKnownLocation;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fine = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarse = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (Boolean.TRUE.equals(fine) || Boolean.TRUE.equals(coarse)) {
                    loadMapData();
                } else {
                    Toast.makeText(this, R.string.location_permission_required, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE));

        setupToolbarAndDrawer();
        setupRecycler();
        setupSearchAndFilter();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        openTripMapRepository = new OpenTripMapRepository(this);
        customPlaceRepository = new CustomPlaceRepository(this);

        binding.mapView.setTileSource(TileSourceFactory.MAPNIK);
        binding.mapView.setMultiTouchControls(true);

        checkLocationPermissionAndLoad();
    }

    private void setupToolbarAndDrawer() {
        binding.buttonMenu.setOnClickListener(v -> binding.drawerLayout.openDrawer(GravityCompat.START));
        binding.navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupRecycler() {
        adapter = new TouristPlaceAdapter(place -> {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(Constants.EXTRA_PLACE, place);
            startActivity(intent);
        });
        binding.recyclerPlaces.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerPlaces.setAdapter(adapter);
    }

    private void setupSearchAndFilter() {
        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String kinds = mapQueryToKinds(query);
                loadPlaces(kinds);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });

        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.categories,
                android.R.layout.simple_spinner_item
        );
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(filterAdapter);
        binding.spinnerCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                String kinds = mapCategoryToKinds(selected);
                loadPlaces(kinds);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private String mapQueryToKinds(String query) {
        String q = query.toLowerCase().trim();
        if (q.contains("restaurante") || q.contains("comida") || q.contains("almoço")) return "restaurants,cafes,fast_food";
        if (q.contains("hotel") || q.contains("pousada") || q.contains("dormir")) return "accomodations";
        if (q.contains("museu") || q.contains("historia")) return "museums,historic";
        if (q.contains("natureza") || q.contains("parque")) return "natural";
        return "interesting_places";
    }

    private String mapCategoryToKinds(String category) {
        switch (category) {
            case "Natural": return "natural";
            case "Cultural": return "museums,theatres,amusements";
            case "Historic": return "historic,monuments";
            case "Religion": return "religion";
            case "Personalizado": return "custom";
            default: return "interesting_places";
        }
    }

    private void checkLocationPermissionAndLoad() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            loadMapData();
            return;
        }

        permissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void loadMapData() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) {
                requestCurrentLocation();
                return;
            }
            lastKnownLocation = location;
            renderMap(location);
            loadPlaces("interesting_places");
        }).addOnFailureListener(e -> Toast.makeText(this, "Erro ao obter localização: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void requestCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        com.google.android.gms.location.LocationRequest locationRequest = com.google.android.gms.location.LocationRequest.create()
                .setPriority(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setNumUpdates(1);

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new com.google.android.gms.location.LocationCallback() {
            @Override
            public void onLocationResult(@NonNull com.google.android.gms.location.LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    lastKnownLocation = location;
                    renderMap(location);
                    loadPlaces("interesting_places");
                } else {
                    Toast.makeText(MainActivity.this, "Localização ainda indisponível. Ative o GPS.", Toast.LENGTH_SHORT).show();
                }
            }
        }, Looper.getMainLooper());
    }

    private void renderMap(Location location) {
        GeoPoint center = new GeoPoint(location.getLatitude(), location.getLongitude());
        binding.mapView.getController().setZoom(13.5);
        binding.mapView.getController().setCenter(center);
        binding.mapView.getOverlays().clear();

        Marker userMarker = new Marker(binding.mapView);
        userMarker.setPosition(center);
        userMarker.setTitle(getString(R.string.my_location));
        binding.mapView.getOverlays().add(userMarker);
        binding.mapView.invalidate();
    }

    private void loadPlaces(String kinds) {
        if (lastKnownLocation == null) return;

        if (!NetworkUtils.isOnline(this)) {
            Toast.makeText(this, R.string.offline_using_cache, Toast.LENGTH_SHORT).show();
        }

        openTripMapRepository.getNearbyPlaces(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), kinds, new OpenTripMapRepository.PlacesCallback() {
            @Override
            public void onSuccess(List<TouristPlace> places) {
                List<TouristPlace> merged = new ArrayList<>(places);
                if (kinds.equals("custom") || kinds.equals("interesting_places")) {
                    merged.addAll(mapCustomPlaces());
                }
                adapter.submitList(merged);
                updateMapMarkers(merged);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateMapMarkers(List<TouristPlace> places) {
        // Clear previous place markers but keep user marker
        List<org.osmdroid.views.overlay.Overlay> overlays = binding.mapView.getOverlays();
        for (int i = overlays.size() - 1; i >= 0; i--) {
            if (overlays.get(i) instanceof Marker) {
                Marker m = (Marker) overlays.get(i);
                if (!m.getTitle().equals(getString(R.string.my_location))) {
                    overlays.remove(i);
                }
            }
        }

        for (TouristPlace place : places) {
            Marker marker = new Marker(binding.mapView);
            marker.setPosition(new GeoPoint(place.getLatitude(), place.getLongitude()));
            marker.setTitle(place.getDisplayName());
            marker.setSubDescription(place.getCategory());
            marker.setOnMarkerClickListener((marker1, mapView) -> {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra(Constants.EXTRA_PLACE, place);
                startActivity(intent);
                return true;
            });
            binding.mapView.getOverlays().add(marker);
        }
        binding.mapView.invalidate();
    }

    private List<TouristPlace> mapCustomPlaces() {
        List<TouristPlace> result = new ArrayList<>();
        for (CustomPlace custom : customPlaceRepository.getAll()) {
            TouristPlace tourist = new TouristPlace();
            tourist.setCustom(true);
            tourist.setId(custom.getId());
            tourist.setName(custom.getName());
            tourist.setCategory(custom.getCategory());
            tourist.setLatitude(custom.getLatitude());
            tourist.setLongitude(custom.getLongitude());
            tourist.setImage(custom.getImage());
            tourist.setDescription(custom.getHistory());
            tourist.setDistance(0);
            result.add(tourist);
        }
        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
        if (id == R.id.nav_favorites) {
            startActivity(new Intent(this, FavoritesActivity.class));
        } else if (id == R.id.nav_my_places) {
            startActivity(new Intent(this, MyPlacesActivity.class));
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_logout) {
            // Sign out both Firebase and local session
            try {
                FirebaseAuth.getInstance().signOut();
            } catch (Exception ignored) {}
            new com.example.roraitour.repositories.AuthLocalRepository(this).setLoggedOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}

