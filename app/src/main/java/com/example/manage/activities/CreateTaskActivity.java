package com.example.manage.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.manage.Employee;
import com.example.manage.R;
import com.example.manage.SharedPreferencesHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CreateTaskActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText titleEditText;
    private EditText descriptionEditText;
    private Spinner prioritySpinner;
    private Spinner employeeSpinner;
    private EditText dueDateEditText;
    private Button createButton;
    private Button cancelButton;
    private ProgressBar progressBar;

    private XMLDataManager xmlDataManager;
    private SharedPreferencesHelper prefsHelper;
    private Calendar selectedDate;
    private List<Employee> employeeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        initializeViews();
        checkPermissions();
        loadEmployees();
        setupSpinners();
        setupListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Créer une Tâche");

        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        prioritySpinner = findViewById(R.id.prioritySpinner);
        employeeSpinner = findViewById(R.id.employeeSpinner);
        dueDateEditText = findViewById(R.id.dueDateEditText);
        createButton = findViewById(R.id.createButton);
        cancelButton = findViewById(R.id.cancelButton);
        progressBar = findViewById(R.id.progressBar);

        selectedDate = Calendar.getInstance();

        String xmlPath = getFilesDir() + "/tasks_data.xml";
        xmlDataManager = new XMLDataManager(xmlPath);
        prefsHelper = new SharedPreferencesHelper(this);
    }

    private void checkPermissions() {
        String userRole = prefsHelper.getUserSession().getRole();
        if (!"SUPERVISOR".equals(userRole)) {
            Toast.makeText(this, "Accès refusé: Réservé aux superviseurs", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadEmployees() {
        employeeList = xmlDataManager.getAllEmployees();

        if (employeeList == null || employeeList.isEmpty()) {
            Toast.makeText(this, "Aucun employé disponible", Toast.LENGTH_SHORT).show();
            employeeSpinner.setEnabled(false);
            createButton.setEnabled(false);
        }
    }

    private void setupSpinners() {
        // Priority Spinner
        String[] priorities = {"BASSE", "MOYENNE", "HAUTE", "URGENTE"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, priorities);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(priorityAdapter);
        prioritySpinner.setSelection(1); // MOYENNE par défaut

        // Employee Spinner
        if (employeeList != null && !employeeList.isEmpty()) {
            String[] employeeNames = new String[employeeList.size()];
            for (int i = 0; i < employeeList.size(); i++) {
                employeeNames[i] = employeeList.get(i).getFullName();
            }

            ArrayAdapter<String> employeeAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, employeeNames);
            employeeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            employeeSpinner.setAdapter(employeeAdapter);
        }
    }

    private void setupListeners() {
        dueDateEditText.setOnClickListener(v -> showDatePicker());

        createButton.setOnClickListener(v -> attemptCreateTask());

        cancelButton.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    updateDateDisplay();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        // Ne pas autoriser les dates passées
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        dueDateEditText.setText(sdf.format(selectedDate.getTime()));
    }

    private void attemptCreateTask() {
        // Réinitialiser les erreurs
        titleEditText.setError(null);
        descriptionEditText.setError(null);
        dueDateEditText.setError(null);

        // Récupérer les valeurs
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String dueDate = dueDateEditText.getText().toString().trim();

        // Validation
        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(dueDate)) {
            dueDateEditText.setError("Veuillez sélectionner une date");
            focusView = dueDateEditText;
            cancel = true;
        }

        if (TextUtils.isEmpty(description)) {
            descriptionEditText.setError("Ce champ est requis");
            focusView = descriptionEditText;
            cancel = true;
        }

        if (TextUtils.isEmpty(title)) {
            titleEditText.setError("Ce champ est requis");
            focusView = titleEditText;
            cancel = true;
        } else if (title.length() < 3) {
            titleEditText.setError("Le titre doit contenir au moins 3 caractères");
            focusView = titleEditText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            createTask(title, description, dueDate);
        }
    }

    private void createTask(String title, String description, String dueDate) {
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Simulation du délai

                // Créer l'objet Task
                Task task = new Task();
                task.setId(generateTaskId());
                task.setTitle(title);
                task.setDescription(description);

                // Priorité
                String priorityText = prioritySpinner.getSelectedItem().toString();
                task.setPriority(convertPriorityToEnum(priorityText));

                // Statut par défaut
                task.setStatus("PENDING");

                // Employé assigné
                int selectedEmployeeIndex = employeeSpinner.getSelectedItemPosition();
                if (selectedEmployeeIndex >= 0 && selectedEmployeeIndex < employeeList.size()) {
                    task.setAssignedTo(employeeList.get(selectedEmployeeIndex).getId());
                }

                // Créateur
                task.setCreatedBy(prefsHelper.getUserSession().getId());

                // Dates
                SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE);
                try {
                    String formattedDate = outputFormat.format(inputFormat.parse(dueDate));
                    task.setDueDate(formattedDate);
                } catch (Exception e) {
                    task.setDueDate(dueDate);
                }

                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.FRANCE);
                task.setCreatedDate(dateTimeFormat.format(Calendar.getInstance().getTime()));

                // Enregistrer dans XML
                boolean success = xmlDataManager.addTask(task);

                runOnUiThread(() -> {
                    showProgress(false);

                    if (success) {
                        Toast.makeText(this, "Tâche créée avec succès", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Erreur lors de la création de la tâche", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private String generateTaskId() {
        return "T" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String convertPriorityToEnum(String priorityText) {
        switch (priorityText) {
            case "BASSE": return "LOW";
            case "MOYENNE": return "MEDIUM";
            case "HAUTE": return "HIGH";
            case "URGENTE": return "URGENT";
            default: return "MEDIUM";
        }
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        createButton.setEnabled(!show);
        cancelButton.setEnabled(!show);
        titleEditText.setEnabled(!show);
        descriptionEditText.setEnabled(!show);
        prioritySpinner.setEnabled(!show);
        employeeSpinner.setEnabled(!show);
        dueDateEditText.setEnabled(!show);
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