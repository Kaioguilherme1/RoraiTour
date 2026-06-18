package com.example.roraitour.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.roraitour.R;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            boolean hasFirebaseSession = FirebaseAuth.getInstance().getCurrentUser() != null;
            boolean hasLocalSession = PreferenceManager.getDefaultSharedPreferences(this)
                    .getString("local_user_email", null) != null;

            if (hasFirebaseSession || hasLocalSession) {
                startActivity(new Intent(this, MainActivity.class));
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
            finish();
        }, 1200);
    }
}
