package com.example.roraitour.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.roraitour.R;
import com.example.roraitour.databinding.ActivityLoginBinding;
import com.example.roraitour.models.User;
import com.example.roraitour.repositories.AuthLocalRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private GoogleSignInClient googleSignInClient;
    private AuthLocalRepository localAuth;
    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent data = result.getData();
                if (data == null) {
                    Toast.makeText(this, "Login com Google cancelado.", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                    firebaseAuthWithGoogle(account);
                } catch (ApiException e) {
                    String message = "Erro " + e.getStatusCode() + ": ";
                    if (e.getStatusCode() == 10) {
                        message += "Configuração de desenvolvedor inválida. Verifique o SHA-1 no Firebase.";
                    } else {
                        message += e.getMessage();
                    }
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        localAuth = new AuthLocalRepository(this);

        // Hardcoded Web Client ID from google-services.json for maximum reliability
        String webClientId = "27715946661-7ufri1t6dsah0sbe039htsgo5n6rd8q3.apps.googleusercontent.com";
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.buttonLogin.setOnClickListener(v -> login());
        binding.textRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        binding.buttonGoogle.setOnClickListener(v -> startGoogleSignIn());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void startGoogleSignIn() {
        if (googleSignInClient == null) {
            Toast.makeText(this, "Google Sign-In não configurado. Coloque google-services.json e default_web_client_id.", Toast.LENGTH_LONG).show();
            return;
        }
        googleSignInLauncher.launch(googleSignInClient.getSignInIntent());
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        if (acct == null || acct.getIdToken() == null) {
            Toast.makeText(this, "Conta Google inválida.", Toast.LENGTH_LONG).show();
            return;
        }
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            localAuth.saveUser(user.getUid(), user.getDisplayName(), user.getEmail(), "");
                            firestore.collection("users").document(user.getUid()).set(new com.example.roraitour.models.User(
                                    user.getUid(),
                                    user.getDisplayName(),
                                    user.getEmail(),
                                    String.valueOf(System.currentTimeMillis())
                            )).addOnFailureListener(e ->
                                    Toast.makeText(LoginActivity.this, "Não foi possível sincronizar o perfil.", Toast.LENGTH_SHORT).show());
                        }
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Autenticação Google falhou.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void login() {
        String email = binding.editEmail.getText().toString().trim();
        String password = binding.editPassword.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editEmail.setError("Email inválido");
            return;
        }
        if (password.length() < 6) {
            binding.editPassword.setError("Senha deve ter ao menos 6 caracteres");
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        localAuth.saveUser(user.getUid(), user.getDisplayName(), user.getEmail(), password);
                    }
                    Toast.makeText(this, "Login realizado", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Try local auth fallback
                    new Thread(() -> {
                        User u = localAuth.authenticate(email, password);
                        runOnUiThread(() -> {
                            if (u != null) {
                                Toast.makeText(LoginActivity.this, "Login local realizado", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }).start();
                });
    }
}
