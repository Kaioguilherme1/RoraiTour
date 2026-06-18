package com.example.roraitour.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.roraitour.databinding.ActivityAddPlaceBinding;
import com.example.roraitour.models.CustomPlace;
import com.example.roraitour.repositories.CustomPlaceRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddPlaceActivity extends AppCompatActivity {

    private ActivityAddPlaceBinding binding;
    private CustomPlaceRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPlaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new CustomPlaceRepository(this);
        binding.buttonSave.setOnClickListener(v -> savePlace());
    }

    private void savePlace() {
        String name = binding.editName.getText().toString().trim();
        String description = binding.editDescription.getText().toString().trim();
        String category = binding.editCategory.getText().toString().trim();
        String lat = binding.editLatitude.getText().toString().trim();
        String lon = binding.editLongitude.getText().toString().trim();
        String history = binding.editHistory.getText().toString().trim();
        String image = binding.editImage.getText().toString().trim();

        if (name.isEmpty() || lat.isEmpty() || lon.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha nome, latitude e longitude.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            CustomPlace place = new CustomPlace();
            place.setName(name);
            place.setDescription(description);
            place.setCategory(category.isEmpty() ? "Personalizado" : category);
            place.setLatitude(Double.parseDouble(lat.replace(',', '.')));
            place.setLongitude(Double.parseDouble(lon.replace(',', '.')));
            place.setHistory(history);
            place.setImage(image);
            place.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

            long result = repository.create(place);
            if (result != -1) {
                Toast.makeText(this, "Local adicionado com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Erro ao salvar no banco de dados.", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Latitude e Longitude devem ser números válidos.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Ocorreu um erro inesperado: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

