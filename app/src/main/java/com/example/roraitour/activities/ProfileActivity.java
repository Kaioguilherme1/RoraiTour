package com.example.roraitour.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.roraitour.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            binding.textName.setText(user.getDisplayName() == null ? "—" : user.getDisplayName());
            binding.textEmail.setText(user.getEmail());
        } else {
            String localName = PreferenceManager.getDefaultSharedPreferences(this).getString("local_user_name", null);
            String localEmail = PreferenceManager.getDefaultSharedPreferences(this).getString("local_user_email", null);
            binding.textName.setText(localName == null || localName.isEmpty() ? "Usuário local" : localName);
            binding.textEmail.setText(localEmail == null || localEmail.isEmpty() ? "—" : localEmail);
        }

        binding.buttonSignOut.setOnClickListener(v -> {
            try {
                FirebaseAuth.getInstance().signOut();
            } catch (Exception ignored) {}
            new com.example.roraitour.repositories.AuthLocalRepository(this).setLoggedOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
