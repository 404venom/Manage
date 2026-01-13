package com.example.manage;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manage.activities.CreateTaskActivity;
import com.example.manage.activities.TaskDetailActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class DashboardFragment extends Fragment {

    private TextView totalTasksTextView;
    private TextView pendingTasksTextView;
    private TextView inProgressTasksTextView;
    private TextView completedTasksTextView;
    private TextView totalEmployeesTextView;
    private TextView activeEmployeesTextView;
    private TextView completionRateTextView;
    private TextView urgentTasksTextView;

    private CardView pendingCard;
    private CardView inProgressCard;
    private CardView completedCard;
    private CardView urgentCard;

    private RecyclerView recentTasksRecyclerView;
    private TextView emptyRecentTextView;
    private FloatingActionButton fabCreateTask;

    private XMLDataManager xmlDataManager;
    private SharedPreferencesHelper prefsHelper;
    private TaskAdapter taskAdapter;
    private View progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        initializeViews(view);
        setupListeners();
        loadStatistics();
        loadRecentTasks();

        return view;
    }

    private void initializeViews(View view) {
        // Statistiques principales
        totalTasksTextView = view.findViewById(R.id.totalTasksTextView);
        pendingTasksTextView = view.findViewById(R.id.pendingTasksTextView);
        inProgressTasksTextView = view.findViewById(R.id.inProgressTasksTextView);
        completedTasksTextView = view.findViewById(R.id.completedTasksTextView);
        totalEmployeesTextView = view.findViewById(R.id.totalEmployeesTextView);
        activeEmployeesTextView = view.findViewById(R.id.activeEmployeesTextView);
        completionRateTextView = view.findViewById(R.id.completionRateTextView);
        urgentTasksTextView = view.findViewById(R.id.urgentTasksTextView);

        // Cards cliquables
        pendingCard = view.findViewById(R.id.pendingCard);
        inProgressCard = view.findViewById(R.id.inProgressCard);
        completedCard = view.findViewById(R.id.completedCard);
        urgentCard = view.findViewById(R.id.urgentCard);

        // Tâches récentes
        recentTasksRecyclerView = view.findViewById(R.id.recentTasksRecyclerView);
        emptyRecentTextView = view.findViewById(R.id.emptyRecentTextView);
        progressBar = view.findViewById(R.id.progressBar);

        recentTasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // FAB
        fabCreateTask = view.findViewById(R.id.fabCreateTask);

        // Initialisation des managers
        String xmlPath = requireContext().getFilesDir() + "/tasks_data.xml";
        xmlDataManager = new XMLDataManager(xmlPath);
        prefsHelper = new SharedPreferencesHelper(requireContext());
    }

    private void setupListeners() {
        fabCreateTask.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateTaskActivity.class);
            startActivity(intent);
        });

        // Navigation vers les filtres de tâches
        pendingCard.setOnClickListener(v -> navigateToTaskList("PENDING"));
        inProgressCard.setOnClickListener(v -> navigateToTaskList("IN_PROGRESS"));
        completedCard.setOnClickListener(v -> navigateToTaskList("COMPLETED"));
        urgentCard.setOnClickListener(v -> navigateToTaskList("URGENT"));
    }

    private void loadStatistics() {
        showProgress(true);

        new Thread(() -> {
            try {
                // Récupérer les statistiques
                int totalTasks = xmlDataManager.getTotalTasksCount();
                int pendingTasks = xmlDataManager.getTaskCountByStatus("PENDING");
                int inProgressTasks = xmlDataManager.getTaskCountByStatus("IN_PROGRESS");
                int completedTasks = xmlDataManager.getTaskCountByStatus("COMPLETED");
                int urgentTasks = xmlDataManager.getTaskCountByPriority("URGENT");

                int totalEmployees = xmlDataManager.getTotalEmployeesCount();
                int activeEmployees = xmlDataManager.getActiveEmployeesCount();

                // Calculer le taux de complétion
                double completionRate = totalTasks > 0
                        ? (double) completedTasks / totalTasks * 100
                        : 0.0;

                // Mettre à jour l'UI
                requireActivity().runOnUiThread(() -> {
                    totalTasksTextView.setText(String.valueOf(totalTasks));
                    pendingTasksTextView.setText(String.valueOf(pendingTasks));
                    inProgressTasksTextView.setText(String.valueOf(inProgressTasks));
                    completedTasksTextView.setText(String.valueOf(completedTasks));
                    urgentTasksTextView.setText(String.valueOf(urgentTasks));

                    totalEmployeesTextView.setText(String.valueOf(totalEmployees));
                    activeEmployeesTextView.setText(String.valueOf(activeEmployees));

                    completionRateTextView.setText(String.format("%.1f%%", completionRate));

                    showProgress(false);
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    showProgress(false);
                    // Afficher des valeurs par défaut en cas d'erreur
                    totalTasksTextView.setText("0");
                    pendingTasksTextView.setText("0");
                    inProgressTasksTextView.setText("0");
                    completedTasksTextView.setText("0");
                    urgentTasksTextView.setText("0");
                    totalEmployeesTextView.setText("0");
                    activeEmployeesTextView.setText("0");
                    completionRateTextView.setText("0%");
                });
            }
        }).start();
    }

    private void loadRecentTasks() {
        new Thread(() -> {
            try {
                // Récupérer les 5 tâches les plus récentes
                List<Task> recentTasks = xmlDataManager.getRecentTasks(5);

                requireActivity().runOnUiThread(() -> {
                    if (recentTasks == null || recentTasks.isEmpty()) {
                        emptyRecentTextView.setVisibility(View.VISIBLE);
                        recentTasksRecyclerView.setVisibility(View.GONE);
                    } else {
                        emptyRecentTextView.setVisibility(View.GONE);
                        recentTasksRecyclerView.setVisibility(View.VISIBLE);

                        taskAdapter = new TaskAdapter(getContext(), recentTasks, task -> {
                            Intent intent = new Intent(getActivity(), TaskDetailActivity.class);
                            intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, task.getId());
                            startActivity(intent);
                        });

                        recentTasksRecyclerView.setAdapter(taskAdapter);
                    }
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    emptyRecentTextView.setVisibility(View.VISIBLE);
                    recentTasksRecyclerView.setVisibility(View.GONE);
                });
            }
        }).start();
    }

    private void navigateToTaskList(String filter) {
        // Naviguer vers TaskListFragment avec filtre
        TaskListFragment fragment = TaskListFragment.newInstance(filter);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Rafraîchir les données au retour
        loadStatistics();
        loadRecentTasks();
    }
}