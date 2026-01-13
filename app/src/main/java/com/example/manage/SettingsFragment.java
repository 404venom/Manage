package com.example.manage;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.manage.activities.LoginActivity;

public class SettingsFragment extends Fragment {

    private Switch notificationsSwitch;
    private Switch soundSwitch;
    private Switch vibrationSwitch;
    private Switch darkModeSwitch;
    private CardView accountCard;
    private CardView dataCard;
    private CardView aboutCard;
    private CardView logoutCard;
    private TextView versionTextView;

    private SharedPreferencesHelper prefsHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        initializeViews(view);
        loadSettings();
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        notificationsSwitch = view.findViewById(R.id.notificationsSwitch);
        soundSwitch = view.findViewById(R.id.soundSwitch);
        vibrationSwitch = view.findViewById(R.id.vibrationSwitch);
        darkModeSwitch = view.findViewById(R.id.darkModeSwitch);
        accountCard = view.findViewById(R.id.accountCard);
        dataCard = view.findViewById(R.id.dataCard);
        aboutCard = view.findViewById(R.id.aboutCard);
        logoutCard = view.findViewById(R.id.logoutCard);
        versionTextView = view.findViewById(R.id.versionTextView);

        prefsHelper = new SharedPreferencesHelper(requireContext());

        versionTextView.setText("Version 1.0.0");
    }

    private void loadSettings() {
        // Charger les préférences sauvegardées
        notificationsSwitch.setChecked(prefsHelper.getBoolean("notifications_enabled", true));
        soundSwitch.setChecked(prefsHelper.getBoolean("sound_enabled", true));
        vibrationSwitch.setChecked(prefsHelper.getBoolean("vibration_enabled", true));
        darkModeSwitch.setChecked(prefsHelper.getBoolean("dark_mode_enabled", false));
    }

    private void setupListeners() {
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsHelper.saveBoolean("notifications_enabled", isChecked);
        });

        soundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsHelper.saveBoolean("sound_enabled", isChecked);
        });

        vibrationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsHelper.saveBoolean("vibration_enabled", isChecked);
        });

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsHelper.saveBoolean("dark_mode_enabled", isChecked);
            showRestartDialog();
        });

        accountCard.setOnClickListener(v -> showAccountSettings());
        dataCard.setOnClickListener(v -> showDataManagement());
        aboutCard.setOnClickListener(v -> showAboutDialog());
        logoutCard.setOnClickListener(v -> showLogoutDialog());
    }

    private void showAccountSettings() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Paramètres du compte")
                .setMessage("Fonctionnalité en développement")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showDataManagement() {
        String[] options = {"Exporter les données", "Importer les données", "Effacer les données"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Gestion des données")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            exportData();
                            break;
                        case 1:
                            importData();
                            break;
                        case 2:
                            confirmClearData();
                            break;
                    }
                })
                .show();
    }

    private void exportData() {
        android.widget.Toast.makeText(getContext(),
                "Export des données en développement",
                android.widget.Toast.LENGTH_SHORT).show();
    }

    private void importData() {
        android.widget.Toast.makeText(getContext(),
                "Import des données en développement",
                android.widget.Toast.LENGTH_SHORT).show();
    }

    private void confirmClearData() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Effacer les données")
                .setMessage("Êtes-vous sûr de vouloir effacer toutes les données ? Cette action est irréversible.")
                .setPositiveButton("Oui", (dialog, which) -> clearData())
                .setNegativeButton("Non", null)
                .show();
    }

    private void clearData() {
        android.widget.Toast.makeText(getContext(),
                "Données effacées",
                android.widget.Toast.LENGTH_SHORT).show();
    }

    private void showAboutDialog() {
        View aboutView = getLayoutInflater().inflate(R.layout.dialog_about, null);

        new AlertDialog.Builder(requireContext())
                .setTitle("À propos")
                .setView(aboutView)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Déconnexion")
                .setMessage("Voulez-vous vraiment vous déconnecter ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    prefsHelper.clearUserSession();

                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void showRestartDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Redémarrage requis")
                .setMessage("L'application doit redémarrer pour appliquer le thème sombre.")
                .setPositiveButton("Redémarrer", (dialog, which) -> {
                    requireActivity().recreate();
                })
                .setNegativeButton("Plus tard", null)
                .show();
    }
}