package com.example.roraitour.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.roraitour.databinding.ActivityRegisterBinding;
import com.example.roraitour.repositories.AuthLocalRepository;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AuthLocalRepository localAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        localAuth = new AuthLocalRepository(this);

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

        new Thread(() -> {
            long result = localAuth.saveUser(java.util.UUID.randomUUID().toString(), name, email, password);
            runOnUiThread(() -> {
                if (result != -1) {
                    Toast.makeText(this, "Cadastro realizado", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finishAffinity();
                } else {
                    Toast.makeText(this, "Erro ao realizar cadastro", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}
