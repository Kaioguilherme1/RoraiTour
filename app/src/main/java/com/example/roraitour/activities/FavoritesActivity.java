package com.example.roraitour.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.roraitour.adapters.FavoriteAdapter;
import com.example.roraitour.databinding.ActivityFavoritesBinding;
import com.example.roraitour.models.FavoritePlace;
import com.example.roraitour.models.TouristPlace;
import com.example.roraitour.repositories.FavoriteRepository;
import com.example.roraitour.utils.Constants;

public class FavoritesActivity extends AppCompatActivity {

    private ActivityFavoritesBinding binding;
    private FavoriteAdapter adapter;
    private FavoriteRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavoritesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new FavoriteRepository(this);
        adapter = new FavoriteAdapter(this::openDetails);
        binding.recyclerFavorites.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerFavorites.setAdapter(adapter);
        binding.fabAddPlace.setOnClickListener(v -> startActivity(new Intent(this, AddPlaceActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.submitList(repository.getAll());
    }

    private void openDetails(FavoritePlace favoritePlace) {
        TouristPlace place = new TouristPlace();
        place.setXid(favoritePlace.getXid());
        place.setName(favoritePlace.getName());
        place.setCategory(favoritePlace.getCategory());
        place.setLatitude(favoritePlace.getLatitude());
        place.setLongitude(favoritePlace.getLongitude());
        place.setImage(favoritePlace.getImage());
        place.setDescription(favoritePlace.getDescription());
        place.setDistance(favoritePlace.getDistance());

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(Constants.EXTRA_PLACE, place);
        startActivity(intent);
    }
}

