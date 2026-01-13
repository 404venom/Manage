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
import com.google.android.material.chip.Chip;

public class TaskDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "task_id";

    private Toolbar toolbar;
    private TextView titleTextView;
    private TextView descriptionTextView;
    private TextView assignedToTextView;
    private TextView createdByTextView;
    private TextView dueDateTextView;
    private TextView createdDateTextView;
    private Chip statusChip;
    private Chip priorityChip;
    private Button startTaskButton;
    private Button completeTaskButton;
    private Button cancelTaskButton;
    private ImageView taskIconImageView;

    private XMLDataManager xmlDataManager;
    private SharedPreferencesHelper prefsHelper;
    private Task currentTask;
    private String taskId;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        initializeViews();
        loadTaskData();
        setupListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Détails de la Tâche");

        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        assignedToTextView = findViewById(R.id.assignedToTextView);
        createdByTextView = findViewById(R.id.createdByTextView);
        dueDateTextView = findViewById(R.id.dueDateTextView);
        createdDateTextView = findViewById(R.id.createdDateTextView);
        statusChip = findViewById(R.id.statusChip);
        priorityChip = findViewById(R.id.priorityChip);
        taskIconImageView = findViewById(R.id.taskIconImageView);

        startTaskButton = findViewById(R.id.startTaskButton);
        completeTaskButton = findViewById(R.id.completeTaskButton);
        cancelTaskButton = findViewById(R.id.cancelTaskButton);

        // Initialisation des données
        String xmlPath = getFilesDir() + "/tasks_data.xml";
        xmlDataManager = new XMLDataManager(xmlPath);
        prefsHelper = new SharedPreferencesHelper(this);
        userRole = prefsHelper.getUserSession().getRole();
    }

    private void loadTaskData() {
        taskId = getIntent().getStringExtra(EXTRA_TASK_ID);

        if (taskId == null) {
            Toast.makeText(this, "Erreur: ID de tâche invalide", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentTask = xmlDataManager.getTaskById(taskId);

        if (currentTask == null) {
            Toast.makeText(this, "Tâche introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        displayTaskDetails();
        updateButtonsVisibility();
    }

    private void displayTaskDetails() {
        titleTextView.setText(currentTask.getTitle());
        descriptionTextView.setText(currentTask.getDescription());
        assignedToTextView.setText("Assigné à: " + currentTask.getAssignedToName());
        createdByTextView.setText("Créé par: " + currentTask.getCreatedByName());
        dueDateTextView.setText("Date limite: " + formatDate(currentTask.getDueDate()));
        createdDateTextView.setText("Créé le: " + formatDateTime(currentTask.getCreatedDate()));

        // Status Chip
        statusChip.setText(getStatusText(currentTask.getStatus()));
        statusChip.setChipBackgroundColorResource(getStatusColor(currentTask.getStatus()));

        // Priority Chip
        priorityChip.setText(getPriorityText(currentTask.getPriority()));
        priorityChip.setChipBackgroundColorResource(getPriorityColor(currentTask.getPriority()));

        // Icône selon la priorité
        taskIconImageView.setImageResource(getPriorityIcon(currentTask.getPriority()));
    }

    private void updateButtonsVisibility() {
        String status = currentTask.getStatus();
        boolean isEmployee = "EMPLOYEE".equals(userRole);
        boolean isSupervisor = "SUPERVISOR".equals(userRole);

        // Boutons visibles uniquement pour les employés assignés ou superviseurs
        if (isEmployee) {
            String currentUserId = prefsHelper.getUserSession().getId();
            boolean isAssigned = currentTask.getAssignedTo().equals(currentUserId);

            if (!isAssigned) {
                startTaskButton.setVisibility(View.GONE);
                completeTaskButton.setVisibility(View.GONE);
                cancelTaskButton.setVisibility(View.GONE);
                return;
            }

            switch (status) {
                case "PENDING":
                    startTaskButton.setVisibility(View.VISIBLE);
                    completeTaskButton.setVisibility(View.GONE);
                    cancelTaskButton.setVisibility(View.GONE);
                    break;
                case "IN_PROGRESS":
                    startTaskButton.setVisibility(View.GONE);
                    completeTaskButton.setVisibility(View.VISIBLE);
                    cancelTaskButton.setVisibility(View.GONE);
                    break;
                case "COMPLETED":
                case "CANCELLED":
                    startTaskButton.setVisibility(View.GONE);
                    completeTaskButton.setVisibility(View.GONE);
                    cancelTaskButton.setVisibility(View.GONE);
                    break;
            }
        } else if (isSupervisor) {
            // Superviseur peut annuler n'importe quelle tâche non terminée
            startTaskButton.setVisibility(View.GONE);
            completeTaskButton.setVisibility(View.GONE);
            cancelTaskButton.setVisibility(
                    "COMPLETED".equals(status) || "CANCELLED".equals(status)
                            ? View.GONE : View.VISIBLE
            );
        }
    }

    private void setupListeners() {
        startTaskButton.setOnClickListener(v -> updateTaskStatus("IN_PROGRESS"));
        completeTaskButton.setOnClickListener(v -> showCompleteDialog());
        cancelTaskButton.setOnClickListener(v -> showCancelDialog());
    }

    private void showCompleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Marquer comme terminée")
                .setMessage("Êtes-vous sûr d'avoir terminé cette tâche ?")
                .setPositiveButton("Oui", (dialog, which) -> updateTaskStatus("COMPLETED"))
                .setNegativeButton("Non", null)
                .show();
    }

    private void showCancelDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Annuler la tâche")
                .setMessage("Voulez-vous vraiment annuler cette tâche ?")
                .setPositiveButton("Oui", (dialog, which) -> updateTaskStatus("CANCELLED"))
                .setNegativeButton("Non", null)
                .show();
    }

    private void updateTaskStatus(String newStatus) {
        boolean success = xmlDataManager.updateTaskStatus(taskId, newStatus);

        if (success) {
            Toast.makeText(this, "Statut mis à jour avec succès", Toast.LENGTH_SHORT).show();
            currentTask.setStatus(newStatus);
            displayTaskDetails();
            updateButtonsVisibility();

            // Notifier l'activité parente du changement
            setResult(RESULT_OK);
        } else {
            Toast.makeText(this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
        }
    }

    // Méthodes utilitaires
    private String getStatusText(String status) {
        switch (status) {
            case "PENDING": return "En attente";
            case "IN_PROGRESS": return "En cours";
            case "COMPLETED": return "Terminée";
            case "CANCELLED": return "Annulée";
            default: return status;
        }
    }

    private int getStatusColor(String status) {
        switch (status) {
            case "PENDING": return R.color.status_pending;
            case "IN_PROGRESS": return R.color.status_in_progress;
            case "COMPLETED": return R.color.status_completed;
            case "CANCELLED": return R.color.status_cancelled;
            default: return R.color.grey;
        }
    }

    private String getPriorityText(String priority) {
        switch (priority) {
            case "LOW": return "Basse";
            case "MEDIUM": return "Moyenne";
            case "HIGH": return "Haute";
            case "URGENT": return "Urgente";
            default: return priority;
        }
    }

    private int getPriorityColor(String priority) {
        switch (priority) {
            case "LOW": return R.color.priority_low;
            case "MEDIUM": return R.color.priority_medium;
            case "HIGH": return R.color.priority_high;
            case "URGENT": return R.color.priority_urgent;
            default: return R.color.grey;
        }
    }

    private int getPriorityIcon(String priority) {
        switch (priority) {
            case "URGENT": return R.drawable.ic_priority_urgent;
            case "HIGH": return R.drawable.ic_priority_high;
            case "MEDIUM": return R.drawable.ic_priority_medium;
            default: return R.drawable.ic_priority_low;
        }
    }

    private String formatDate(String date) {
        // Format: YYYY-MM-DD -> DD/MM/YYYY
        if (date != null && date.length() >= 10) {
            String[] parts = date.split("-");
            return parts[2] + "/" + parts[1] + "/" + parts[0];
        }
        return date;
    }

    private String formatDateTime(String dateTime) {
        // Format: YYYY-MM-DDTHH:MM:SS -> DD/MM/YYYY à HH:MM
        if (dateTime != null && dateTime.length() >= 19) {
            String[] parts = dateTime.split("T");
            String date = formatDate(parts[0]);
            String time = parts[1].substring(0, 5);
            return date + " à " + time;
        }
        return dateTime;
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