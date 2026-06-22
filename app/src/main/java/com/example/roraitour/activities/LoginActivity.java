package com.example.roraitour.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.roraitour.databinding.ActivityLoginBinding;
import com.example.roraitour.models.User;
import com.example.roraitour.repositories.AuthLocalRepository;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthLocalRepository localAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        localAuth = new AuthLocalRepository(this);

        binding.buttonLogin.setOnClickListener(v -> login());
        binding.textRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (localAuth.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void login() {
        if (binding.editEmail.getText() == null || binding.editPassword.getText() == null) return;

        String email = binding.editEmail.getText().toString().trim();
        String password = binding.editPassword.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editEmail.setError("Email inválido");
            return;
        }
        if (password.length() < 6) {
            binding.editPassword.setError("Senha deve ter ao menos 6 caracteres");
            return;
        }

        new Thread(() -> {
            User u = localAuth.authenticate(email, password);
            runOnUiThread(() -> {
                if (u != null) {
                    Toast.makeText(LoginActivity.this, "Login realizado", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Usuário ou senha incorretos", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }
}
