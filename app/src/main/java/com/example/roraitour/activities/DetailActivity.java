package com.example.roraitour.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.roraitour.R;
import com.example.roraitour.databinding.ActivityDetailBinding;
import com.example.roraitour.models.TouristPlace;
import com.example.roraitour.repositories.CustomPlaceRepository;
import com.example.roraitour.repositories.FavoriteRepository;
import com.example.roraitour.repositories.OpenTripMapRepository;
import com.example.roraitour.repositories.WikipediaRepository;
import com.example.roraitour.utils.Constants;
import com.example.roraitour.utils.ImageLoader;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private TouristPlace place;
    private FavoriteRepository favoriteRepository;
    private CustomPlaceRepository customPlaceRepository;
    private OpenTripMapRepository openTripMapRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        place = (TouristPlace) getIntent().getSerializableExtra(Constants.EXTRA_PLACE);
        if (place == null) {
            finish();
            return;
        }

        favoriteRepository = new FavoriteRepository(this);
        customPlaceRepository = new CustomPlaceRepository(this);
        openTripMapRepository = new OpenTripMapRepository(this);

        setupUi();
        setupButtons();
        loadDetails();
    }

    private void setupUi() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.textName.setText(place.getDisplayName());
        binding.textCategory.setText(place.getCategory());
        binding.textCoordinates.setText(getString(R.string.coordinates_value, place.getLatitude(), place.getLongitude()));
        binding.textDistance.setText(getString(R.string.distance_km, place.getDistance() / 1000d));
        binding.textDescription.setText(place.getDescription() == null || place.getDescription().isEmpty()
                ? getString(R.string.history_unavailable)
                : place.getDescription());
        ImageLoader.load(binding.imagePlace, place.getImage());

        int customVisibility = place.isCustom() ? android.view.View.VISIBLE : android.view.View.GONE;
        binding.buttonEdit.setVisibility(customVisibility);
        binding.buttonDelete.setVisibility(customVisibility);
    }

    private void setupButtons() {
        binding.buttonFavorite.setOnClickListener(v -> toggleFavorite());
        binding.buttonShare.setOnClickListener(v -> sharePlace());
        binding.buttonRoute.setOnClickListener(v -> openRoute());

        binding.buttonEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditPlaceActivity.class);
            intent.putExtra(Constants.EXTRA_CUSTOM_PLACE_ID, place.getId());
            startActivity(intent);
        });

        binding.buttonDelete.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle(R.string.confirm_delete_title)
                        .setMessage(R.string.confirm_delete_message)
                        .setPositiveButton(R.string.delete, (dialog, which) -> {
                            customPlaceRepository.delete(place.getId());
                            Toast.makeText(this, R.string.place_deleted, Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show()
        );
    }

    private void toggleFavorite() {
        boolean isFavorite = favoriteRepository.isFavorite(place.getXid(), place.getDisplayName());
        if (isFavorite) {
            favoriteRepository.deleteByPlace(place);
            Toast.makeText(this, "Removido dos favoritos", Toast.LENGTH_SHORT).show();
        } else {
            long id = favoriteRepository.save(place);
            if (id != -1) {
                Toast.makeText(this, "Adicionado aos favoritos!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Erro ao favoritar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sharePlace() {
        String text = place.getDisplayName() + "\n"
                + getString(R.string.coordinates_value, place.getLatitude(), place.getLongitude()) + "\n"
                + place.getCategory();

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(sendIntent, getString(R.string.share_place)));
    }

    private void openRoute() {
        // Tenta abrir no Google Maps primeiro
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + place.getLatitude() + "," + place.getLongitude());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Fallback para qualquer app de mapas (incluindo Apple Maps se fosse iOS, ou outros no Android)
            Uri genericUri = Uri.parse("geo:" + place.getLatitude() + "," + place.getLongitude() + "?q=" + place.getLatitude() + "," + place.getLongitude() + "(" + place.getDisplayName() + ")");
            Intent intent = new Intent(Intent.ACTION_VIEW, genericUri);
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Nenhum aplicativo de mapas encontrado.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadDetails() {
        if (place.isCustom()) {
            return;
        }

        binding.progressLoading.setVisibility(android.view.View.VISIBLE);
        // Primeiro tentamos pegar detalhes completos via OpenTripMap (mais preciso usando XID)
        openTripMapRepository.getPlaceDetails(place.getXid(), new OpenTripMapRepository.DetailsCallback() {
            @Override
            public void onSuccess(com.example.roraitour.api.OpenTripMapDetailResponse details, String wikipediaExtract, String wikipediaImageUrl) {
                binding.progressLoading.setVisibility(android.view.View.GONE);
                // Preferir extract / image retornados (vêm de OTM ou Wikipedia)
                if (wikipediaExtract != null && !wikipediaExtract.trim().isEmpty()) {
                    binding.textDescription.setText(wikipediaExtract);
                    place.setDescription(wikipediaExtract);
                }

                if (wikipediaImageUrl != null && !wikipediaImageUrl.isEmpty()) {
                    ImageLoader.load(binding.imagePlace, wikipediaImageUrl);
                    place.setImage(wikipediaImageUrl);
                }
            }

            @Override
            public void onError(String message) {
                // Se falhar, tentamos a busca genérica pela Wikipedia como fallback
                loadWikipediaFallback();
            }
        });
    }

    private void loadWikipediaFallback() {
        new WikipediaRepository().fetchSummary(place.getDisplayName(), new WikipediaRepository.WikipediaCallback() {
            @Override
            public void onSuccess(String summary, String imageUrl) {
                binding.progressLoading.setVisibility(android.view.View.GONE);
                binding.textDescription.setText(summary == null || summary.trim().isEmpty()
                        ? getString(R.string.history_unavailable)
                        : summary);
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    ImageLoader.load(binding.imagePlace, imageUrl);
                }
            }

            @Override
            public void onError() {
                binding.progressLoading.setVisibility(android.view.View.GONE);
                binding.textDescription.setText(R.string.history_unavailable);
            }
        });
    }
}

