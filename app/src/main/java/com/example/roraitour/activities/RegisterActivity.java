package com.example.roraitour.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.roraitour.databinding.ActivityRegisterBinding;
import com.example.roraitour.models.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        binding.buttonRegister.setOnClickListener(v -> register());
        binding.textLogin.setOnClickListener(v -> finish());
    }

    private void register() {
        String name = binding.editName.getText().toString().trim();
        String email = binding.editEmail.getText().toString().trim();
        String password = binding.editPassword.getText().toString().trim();
        String confirm = binding.editConfirmPassword.getText().toString().trim();

        if (name.isEmpty()) {
            binding.editName.setError("Informe seu nome");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editEmail.setError("Email inválido");
            return;
        }
        if (password.length() < 6) {
            binding.editPassword.setError("Senha deve ter ao menos 6 caracteres");
            return;
        }
        if (!password.equals(confirm)) {
            binding.editConfirmPassword.setError("As senhas não coincidem");
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser() != null ? authResult.getUser().getUid() : "";
                    String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    User user = new User(uid, name, email, createdAt);
                    firestore.collection("users").document(uid).set(user)
                            .addOnSuccessListener(unused -> {
                                // Save local copy
                                new Thread(() -> new com.example.roraitour.repositories.AuthLocalRepository(RegisterActivity.this)
                                        .saveUser(uid, name, email, password)).start();

                                Toast.makeText(this, "Cadastro realizado", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, MainActivity.class));
                                finishAffinity();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e -> {
                    // If Firebase fails, fall back to local registration
                    new Thread(() -> {
                        new com.example.roraitour.repositories.AuthLocalRepository(RegisterActivity.this).saveUser("", name, email, password);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Cadastro local realizado (offline)", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MainActivity.class));
                            finishAffinity();
                        });
                    }).start();
                });
    }
}

