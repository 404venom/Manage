package com.example.manage.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.manage.R;
import com.example.manage.SharedPreferencesHelper;
import com.example.manage.User;
import com.example.manage.XMLDataManager;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private RadioGroup roleRadioGroup;
    private RadioButton supervisorRadioButton;
    private RadioButton employeeRadioButton;
    private Button loginButton;
    private TextView forgotPasswordTextView;
    private ProgressBar progressBar;

    private XMLDataManager xmlDataManager;
    private SharedPreferencesHelper prefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        initializeData();
        setupListeners();
    }

    private void initializeViews() {
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        roleRadioGroup = findViewById(R.id.roleRadioGroup);
        supervisorRadioButton = findViewById(R.id.supervisorRadioButton);
        employeeRadioButton = findViewById(R.id.employeeRadioButton);
        loginButton = findViewById(R.id.loginButton);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        progressBar = findViewById(R.id.progressBar);

        // Sélection par défaut : Employé
        employeeRadioButton.setChecked(true);
    }

    private void initializeData() {
        String xmlPath = getFilesDir() + "/users_data.xml";
        xmlDataManager = new XMLDataManager(xmlPath);
        prefsHelper = new SharedPreferencesHelper(this);
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> attemptLogin());

        forgotPasswordTextView.setOnClickListener(v -> {
            Toast.makeText(this, "Contactez l'administrateur pour réinitialiser votre mot de passe",
                    Toast.LENGTH_LONG).show();
        });

        // Changement de placeholder selon le rôle
        roleRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.supervisorRadioButton) {
                usernameEditText.setHint("Identifiant Superviseur");
            } else {
                usernameEditText.setHint("Identifiant Employé");
            }
        });
    }

    private void attemptLogin() {
        // Réinitialiser les erreurs
        usernameEditText.setError(null);
        passwordEditText.setError(null);

        // Récupérer les valeurs
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validation
        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Ce champ est requis");
            focusView = passwordEditText;
            cancel = true;
        } else if (password.length() < 4) {
            passwordEditText.setError("Mot de passe trop court (min 4 caractères)");
            focusView = passwordEditText;
            cancel = true;
        }

        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Ce champ est requis");
            focusView = usernameEditText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            performLogin(username, password);
        }
    }

    private void performLogin(String username, String password) {
        // Déterminer le rôle sélectionné
        String role = supervisorRadioButton.isChecked() ? "SUPERVISOR" : "EMPLOYEE";

        // Simulation d'authentification avec XML
        new Thread(() -> {
            try {
                Thread.sleep(1500); // Simulation du délai réseau

                User user = xmlDataManager.authenticateUser(username, password, role);

                runOnUiThread(() -> {
                    showProgress(false);

                    if (user != null) {
                        // Enregistrer la session
                        prefsHelper.saveUserSession(user);

                        // Naviguer vers MainActivity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                        Toast.makeText(this, "Bienvenue, " + user.getFullName(),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Identifiants incorrects",
                                Toast.LENGTH_SHORT).show();
                        passwordEditText.setText("");
                        passwordEditText.requestFocus();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(this, "Erreur de connexion: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!show);
        usernameEditText.setEnabled(!show);
        passwordEditText.setEnabled(!show);
        roleRadioGroup.setEnabled(!show);
    }

    @Override
    public void onBackPressed() {
        // Empêcher le retour arrière sur l'écran de connexion
        moveTaskToBack(true);
    }
}