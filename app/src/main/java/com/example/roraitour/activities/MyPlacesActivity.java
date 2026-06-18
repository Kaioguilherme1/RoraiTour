package com.example.roraitour.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.roraitour.adapters.CustomPlaceAdapter;
import com.example.roraitour.databinding.ActivityMyPlacesBinding;
import com.example.roraitour.repositories.CustomPlaceRepository;
import com.example.roraitour.utils.Constants;

public class MyPlacesActivity extends AppCompatActivity {

    private ActivityMyPlacesBinding binding;
    private CustomPlaceRepository repository;
    private CustomPlaceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyPlacesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new CustomPlaceRepository(this);
        adapter = new CustomPlaceAdapter(place -> {
            Intent intent = new Intent(this, EditPlaceActivity.class);
            intent.putExtra(Constants.EXTRA_CUSTOM_PLACE_ID, place.getId());
            startActivity(intent);
        });

        binding.recyclerMyPlaces.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerMyPlaces.setAdapter(adapter);

        binding.fabAddPlace.setOnClickListener(v -> startActivity(new Intent(this, AddPlaceActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.submitList(repository.getAll());
    }
}

