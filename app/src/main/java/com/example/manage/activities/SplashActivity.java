package com.example.manage.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.manage.R;
import com.example.manage.SharedPreferencesHelper;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 secondes
    private ImageView logoImageView;
    private TextView appNameTextView;
    private SharedPreferencesHelper prefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialisation
        logoImageView = findViewById(R.id.logoImageView);
        appNameTextView = findViewById(R.id.appNameTextView);
        prefsHelper = new SharedPreferencesHelper(this);

        // Animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        logoImageView.startAnimation(fadeIn);
        appNameTextView.startAnimation(slideUp);

        // Navigation après le délai
        new Handler().postDelayed(() -> {
            navigateToNextScreen();
        }, SPLASH_DURATION);
    }

    private void navigateToNextScreen() {
        Intent intent;

        // Vérifier si l'utilisateur est déjà connecté
        if (prefsHelper.isLoggedIn()) {
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}