package com.example.roraitour.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.roraitour.R;
import com.example.roraitour.adapters.TouristPlaceAdapter;
import com.example.roraitour.databinding.ActivityProfileBinding;
import com.example.roraitour.models.TouristPlace;
import com.example.roraitour.repositories.AuthLocalRepository;
import com.example.roraitour.repositories.FavoriteRepository;
import com.example.roraitour.utils.Constants;
import com.example.roraitour.utils.ImageLoader;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private AuthLocalRepository authRepository;
    private FavoriteRepository favoriteRepository;
    private String userEmail;
    private TouristPlaceAdapter favoritesAdapter;
    private Uri cameraImageUri;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        try {
                            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (SecurityException e) {
                            Log.e("Profile", "Erro de permissão", e);
                        }
                        updatePhoto(uri.toString());
                    }
                }
            }
    );

    private final ActivityResultLauncher<Uri> takePhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            result -> {
                if (result && cameraImageUri != null) {
                    updatePhoto(cameraImageUri.toString());
                }
            }
    );

    private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    launchCamera();
                } else {
                    Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authRepository = new AuthLocalRepository(this);
        favoriteRepository = new FavoriteRepository(this);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        userEmail = prefs.getString("local_user_email", "").trim().toLowerCase();

        loadUserData();
        setupFavorites();

        binding.buttonChangePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        binding.buttonTakePhoto.setOnClickListener(v -> cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA));

        binding.buttonUpdateName.setOnClickListener(v -> updateName());
        binding.buttonUpdatePassword.setOnClickListener(v -> updatePassword());

        binding.buttonSignOut.setOnClickListener(v -> {
            authRepository.setLoggedOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadUserData() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String name = prefs.getString("local_user_name", "");
        String image = prefs.getString("local_user_image", null);

        binding.editName.setText(name);
        // Sempre usar ImageLoader para que o placeholder apareça se image for null
        ImageLoader.load(binding.imageProfile, image);

        new Thread(() -> {
            if (!authRepository.userExistsInDatabase(userEmail)) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Banco de dados atualizado. Faça login novamente.", Toast.LENGTH_LONG).show();
                    binding.buttonSignOut.performClick();
                });
            }
        }).start();
    }

    private void launchCamera() {
        try {
            File photoFile = createImageFile();
            cameraImageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            takePhotoLauncher.launch(cameraImageUri);
        } catch (IOException e) {
            Toast.makeText(this, R.string.image_file_error, Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void setupFavorites() {
        favoritesAdapter = new TouristPlaceAdapter(new TouristPlaceAdapter.OnPlaceClickListener() {
            @Override
            public void onPlaceClick(TouristPlace place) {
                Intent intent = new Intent(ProfileActivity.this, DetailActivity.class);
                intent.putExtra(Constants.EXTRA_PLACE, place);
                startActivity(intent);
            }

            @Override
            public void onVisitedChange(TouristPlace place, boolean isVisited) {
                favoriteRepository.updateVisited(place.getXid(), place.getDisplayName(), isVisited);
            }
        });
        binding.recyclerFavorites.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerFavorites.setAdapter(favoritesAdapter);
    }

    private void loadFavorites() {
        new Thread(() -> {
            ArrayList<TouristPlace> list = new ArrayList<>();
            for (com.example.roraitour.models.FavoritePlace fav : favoriteRepository.getAll()) {
                TouristPlace p = new TouristPlace();
                p.setXid(fav.getXid());
                p.setName(fav.getName());
                p.setCategory(fav.getCategory());
                p.setLatitude(fav.getLatitude());
                p.setLongitude(fav.getLongitude());
                p.setImage(fav.getImage());
                p.setDescription(fav.getDescription());
                p.setDistance(fav.getDistance());
                p.setVisited(fav.isVisited());
                list.add(p);
            }
            runOnUiThread(() -> favoritesAdapter.submitList(list));
        }).start();
    }

    private void updatePhoto(String imageUri) {
        if (authRepository.updateProfileImage(userEmail, imageUri)) {
            ImageLoader.load(binding.imageProfile, imageUri);
            Toast.makeText(this, "Foto atualizada!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
        } else {
            Toast.makeText(this, "Erro ao atualizar foto. Tente reiniciar o app.", Toast.LENGTH_LONG).show();
        }
    }

    private void updateName() {
        String newName = binding.editName.getText() != null ? binding.editName.getText().toString().trim() : "";
        if (newName.isEmpty()) {
            binding.editName.setError("Nome não pode ser vazio");
            return;
        }

        if (authRepository.updateName(userEmail, newName)) {
            Toast.makeText(this, "Nome atualizado!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
        } else {
            Toast.makeText(this, "Erro ao atualizar nome.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePassword() {
        String oldPass = binding.editOldPassword.getText() != null ? binding.editOldPassword.getText().toString().trim() : "";
        String newPass = binding.editNewPassword.getText() != null ? binding.editNewPassword.getText().toString().trim() : "";

        if (oldPass.isEmpty() || newPass.isEmpty()) {
            Toast.makeText(this, "Preencha as senhas", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPass.length() < 6) {
            binding.editNewPassword.setError("Mínimo 6 caracteres");
            return;
        }

        if (!authRepository.checkPassword(userEmail, oldPass)) {
            binding.editOldPassword.setError("Senha atual incorreta");
            return;
        }

        if (authRepository.updatePassword(userEmail, newPass)) {
            Toast.makeText(this, "Senha alterada!", Toast.LENGTH_SHORT).show();
            binding.editOldPassword.setText("");
            binding.editNewPassword.setText("");
        } else {
            Toast.makeText(this, "Erro ao alterar senha.", Toast.LENGTH_SHORT).show();
        }
    }
}
