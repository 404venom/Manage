package com.example.manage;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView notificationRecyclerView;
    private TextView emptyTextView;
    private View progressBar;

    private NotificationAdapter notificationAdapter;
    private XMLDataManager xmlDataManager;
    private SharedPreferencesHelper prefsHelper;
    private List<Notification> notificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        initializeViews();
        loadNotifications();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Notifications");

        notificationRecyclerView = findViewById(R.id.notificationRecyclerView);
        emptyTextView = findViewById(R.id.emptyTextView);
        progressBar = findViewById(R.id.progressBar);

        notificationRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        String xmlPath = getFilesDir() + "/notifications_data.xml";
        xmlDataManager = new XMLDataManager(xmlPath);
        prefsHelper = new SharedPreferencesHelper(this);
    }

    private void loadNotifications() {
        showProgress(true);

        new Thread(() -> {
            try {
                Thread.sleep(500); // Simulation du chargement

                String userId = prefsHelper.getUserSession().getId();
                notificationList = xmlDataManager.getNotificationsByUser(userId);

                runOnUiThread(() -> {
                    showProgress(false);
                    displayNotifications();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showProgress(false);
                    showEmptyState(true);
                });
            }
        }).start();
    }

    private void displayNotifications() {
        if (notificationList == null || notificationList.isEmpty()) {
            showEmptyState(true);
            return;
        }

        showEmptyState(false);

        notificationAdapter = new NotificationAdapter(this, notificationList, new NotificationAdapter.OnNotificationClickListener() {
            @Override
            public void onNotificationClick(Notification notification) {
                handleNotificationClick(notification);
            }

            @Override
            public void onDeleteClick(Notification notification) {
                deleteNotification(notification);
            }
        });

        notificationRecyclerView.setAdapter(notificationAdapter);

        // Marquer toutes les notifications comme lues
        markAllAsRead();
    }

    private void handleNotificationClick(Notification notification) {
        // Marquer comme lue
        if (!notification.isRead()) {
            notification.setRead(true);
            xmlDataManager.updateNotification(notification);
            notificationAdapter.notifyDataSetChanged();
        }

        // Rediriger selon le type de notification
        switch (notification.getType()) {
            case "TASK_ASSIGNED":
            case "TASK_UPDATED":
            case "TASK_COMPLETED":
                // Ouvrir les détails de la tâche
                if (notification.getTaskId() != null) {
                    Intent intent = new Intent(this, TaskDetailActivity.class);
                    intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, notification.getTaskId());
                    startActivity(intent);
                }
                break;
            case "REMINDER":
                // Afficher un rappel
                Toast.makeText(this, notification.getMessage(), Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void deleteNotification(Notification notification) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Supprimer la notification")
                .setMessage("Voulez-vous supprimer cette notification ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    boolean success = xmlDataManager.deleteNotification(notification.getId());

                    if (success) {
                        notificationList.remove(notification);
                        notificationAdapter.notifyDataSetChanged();

                        if (notificationList.isEmpty()) {
                            showEmptyState(true);
                        }

                        Toast.makeText(this, "Notification supprimée", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void markAllAsRead() {
        new Thread(() -> {
            for (Notification notification : notificationList) {
                if (!notification.isRead()) {
                    notification.setRead(true);
                    xmlDataManager.updateNotification(notification);
                }
            }
        }).start();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        notificationRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean show) {
        emptyTextView.setVisibility(show ? View.VISIBLE : View.GONE);
        notificationRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recharger les notifications au retour
        if (notificationAdapter != null) {
            loadNotifications();
        }
    }
}