package com.example.manage;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView roleTextView;
    private TextView emailTextView;
    private TextView phoneTextView;
    private TextView departmentTextView;
    private TextView joinDateTextView;
    private TextView taskCountTextView;
    private Button editProfileButton;
    private Button changePasswordButton;
    private Button logoutButton;

    private SharedPreferencesHelper prefsHelper;
    private XMLDataManager xmlDataManager;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeViews();
        loadUserData();
        setupListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Mon Profil");

        profileImageView = findViewById(R.id.profileImageView);
        nameTextView = findViewById(R.id.nameTextView);
        roleTextView = findViewById(R.id.roleTextView);
        emailTextView = findViewById(R.id.emailTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        departmentTextView = findViewById(R.id.departmentTextView);
        joinDateTextView = findViewById(R.id.joinDateTextView);
        taskCountTextView = findViewById(R.id.taskCountTextView);
        editProfileButton = findViewById(R.id.editProfileButton);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        logoutButton = findViewById(R.id.logoutButton);

        String xmlPath = getFilesDir() + "/tasks_data.xml";
        xmlDataManager = new XMLDataManager(xmlPath);
        prefsHelper = new SharedPreferencesHelper(this);
    }

    private void loadUserData() {
        currentUser = prefsHelper.getUserSession();

        if (currentUser == null) {
            Toast.makeText(this, "Erreur: Session expirée", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        displayUserInfo();
        loadStatistics();
    }

    private void displayUserInfo() {
        nameTextView.setText(currentUser.getFullName());

        String roleText = "SUPERVISOR".equals(currentUser.getRole()) ? "Superviseur" : "Employé";
        roleTextView.setText(roleText);

        emailTextView.setText(currentUser.getEmail());
        phoneTextView.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "Non renseigné");
        departmentTextView.setText(currentUser.getDepartment() != null ? currentUser.getDepartment() : "Non renseigné");
        joinDateTextView.setText(currentUser.getJoinDate() != null ? formatDate(currentUser.getJoinDate()) : "Non renseigné");

        // Image de profil selon le rôle
        if ("SUPERVISOR".equals(currentUser.getRole())) {
            profileImageView.setImageResource(R.drawable.ic_supervisor_avatar);
        } else {
            profileImageView.setImageResource(R.drawable.ic_employee_avatar);
        }
    }

    private void loadStatistics() {
        new Thread(() -> {
            int taskCount = 0;

            if ("SUPERVISOR".equals(currentUser.getRole())) {
                // Nombre de tâches créées par le superviseur
                taskCount = xmlDataManager.getTaskCountByCreator(currentUser.getId());
            } else {
                // Nombre de tâches assignées à l'employé
                taskCount = xmlDataManager.getTaskCountByEmployee(currentUser.getId());
            }

            final int finalCount = taskCount;
            runOnUiThread(() -> {
                String statisticsText = "SUPERVISOR".equals(currentUser.getRole())
                        ? finalCount + " tâches créées"
                        : finalCount + " tâches assignées";
                taskCountTextView.setText(statisticsText);
            });
        }).start();
    }

    private void setupListeners() {
        editProfileButton.setOnClickListener(v -> showEditProfileDialog());

        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());

        logoutButton.setOnClickListener(v -> showLogoutDialog());
    }

    private void showEditProfileDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);

        TextInputEditText phoneEditText = dialogView.findViewById(R.id.phoneEditText);
        TextInputEditText departmentEditText = dialogView.findViewById(R.id.departmentEditText);

        phoneEditText.setText(currentUser.getPhone());
        departmentEditText.setText(currentUser.getDepartment());

        new AlertDialog.Builder(this)
                .setTitle("Modifier le profil")
                .setView(dialogView)
                .setPositiveButton("Enregistrer", (dialog, which) -> {
                    String phone = phoneEditText.getText().toString().trim();
                    String department = departmentEditText.getText().toString().trim();

                    updateProfile(phone, department);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void updateProfile(String phone, String department) {
        currentUser.setPhone(phone);
        currentUser.setDepartment(department);

        boolean success = xmlDataManager.updateUser(currentUser);

        if (success) {
            prefsHelper.saveUserSession(currentUser);
            displayUserInfo();
            Toast.makeText(this, "Profil mis à jour avec succès", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
        }
    }

    private void showChangePasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);

        TextInputEditText currentPasswordEditText = dialogView.findViewById(R.id.currentPasswordEditText);
        TextInputEditText newPasswordEditText = dialogView.findViewById(R.id.newPasswordEditText);
        TextInputEditText confirmPasswordEditText = dialogView.findViewById(R.id.confirmPasswordEditText);

        new AlertDialog.Builder(this)
                .setTitle("Changer le mot de passe")
                .setView(dialogView)
                .setPositiveButton("Confirmer", (dialog, which) -> {
                    String currentPassword = currentPasswordEditText.getText().toString();
                    String newPassword = newPasswordEditText.getText().toString();
                    String confirmPassword = confirmPasswordEditText.getText().toString();

                    if (validatePasswordChange(currentPassword, newPassword, confirmPassword)) {
                        changePassword(newPassword);
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private boolean validatePasswordChange(String currentPassword, String newPassword, String confirmPassword) {
        if (!currentPassword.equals(currentUser.getPassword())) {
            Toast.makeText(this, "Mot de passe actuel incorrect", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (newPassword.length() < 4) {
            Toast.makeText(this, "Le nouveau mot de passe doit contenir au moins 4 caractères", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void changePassword(String newPassword) {
        currentUser.setPassword(newPassword);
        boolean success = xmlDataManager.updateUser(currentUser);

        if (success) {
            prefsHelper.saveUserSession(currentUser);
            Toast.makeText(this, "Mot de passe changé avec succès", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Erreur lors du changement de mot de passe", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Déconnexion")
                .setMessage("Voulez-vous vraiment vous déconnecter ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    prefsHelper.clearUserSession();

                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                    Toast.makeText(this, "Déconnexion réussie", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private String formatDate(String date) {
        // Format: YYYY-MM-DD -> DD/MM/YYYY
        if (date != null && date.length() >= 10) {
            String[] parts = date.split("-");
            return parts[2] + "/" + parts[1] + "/" + parts[0];
        }
        return date;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}