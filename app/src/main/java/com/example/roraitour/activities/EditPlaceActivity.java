package com.example.roraitour.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.roraitour.R;
import com.example.roraitour.databinding.ActivityEditPlaceBinding;
import com.example.roraitour.models.CustomPlace;
import com.example.roraitour.repositories.CustomPlaceRepository;
import com.example.roraitour.utils.Constants;

public class EditPlaceActivity extends AppCompatActivity {

    private ActivityEditPlaceBinding binding;
    private CustomPlaceRepository repository;
    private CustomPlace customPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditPlaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new CustomPlaceRepository(this);
        int placeId = getIntent().getIntExtra(Constants.EXTRA_CUSTOM_PLACE_ID, -1);
        customPlace = repository.getById(placeId);

        if (customPlace == null) {
            finish();
            return;
        }

        fillFields();
        binding.buttonSave.setOnClickListener(v -> update());
        binding.buttonDelete.setOnClickListener(v -> confirmDelete());
    }

    private void fillFields() {
        binding.editName.setText(customPlace.getName());
        binding.editDescription.setText(customPlace.getDescription());
        binding.editCategory.setText(customPlace.getCategory());
        binding.editLatitude.setText(String.valueOf(customPlace.getLatitude()));
        binding.editLongitude.setText(String.valueOf(customPlace.getLongitude()));
        binding.editHistory.setText(customPlace.getHistory());
        binding.editImage.setText(customPlace.getImage());
    }

    private void update() {
        String name = binding.editName.getText().toString().trim();
        String lat = binding.editLatitude.getText().toString().trim();
        String lon = binding.editLongitude.getText().toString().trim();

        if (name.isEmpty() || lat.isEmpty() || lon.isEmpty()) {
            Toast.makeText(this, "Nome, latitude e longitude são obrigatórios.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            customPlace.setName(name);
            customPlace.setDescription(binding.editDescription.getText().toString().trim());
            customPlace.setCategory(binding.editCategory.getText().toString().trim());
            customPlace.setLatitude(Double.parseDouble(lat.replace(',', '.')));
            customPlace.setLongitude(Double.parseDouble(lon.replace(',', '.')));
            customPlace.setHistory(binding.editHistory.getText().toString().trim());
            customPlace.setImage(binding.editImage.getText().toString().trim());

            int result = repository.update(customPlace);
            if (result > 0) {
                Toast.makeText(this, "Local atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Erro ao atualizar local.", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Latitude e longitude devem ser números válidos.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    repository.delete(customPlace.getId());
                    Toast.makeText(this, R.string.place_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}

