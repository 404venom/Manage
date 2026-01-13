package com.example.manage;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReportsFragment extends Fragment {

    private TextView reportDateTextView;
    private TextView totalTasksReportTextView;
    private TextView completedTasksReportTextView;
    private TextView pendingTasksReportTextView;
    private TextView cancelledTasksReportTextView;
    private TextView totalEmployeesReportTextView;
    private TextView activeEmployeesReportTextView;
    private TextView avgCompletionTimeTextView;
    private TextView productivityScoreTextView;
    private CardView weeklyCard;
    private CardView monthlyCard;
    private CardView yearlyCard;
    private Button exportButton;
    private View progressBar;

    private XMLDataManager xmlDataManager;
    private String selectedPeriod = "WEEKLY";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        initializeViews(view);
        setupListeners();
        loadReport();

        return view;
    }

    private void initializeViews(View view) {
        reportDateTextView = view.findViewById(R.id.reportDateTextView);
        totalTasksReportTextView = view.findViewById(R.id.totalTasksReportTextView);
        completedTasksReportTextView = view.findViewById(R.id.completedTasksReportTextView);
        pendingTasksReportTextView = view.findViewById(R.id.pendingTasksReportTextView);
        cancelledTasksReportTextView = view.findViewById(R.id.cancelledTasksReportTextView);
        totalEmployeesReportTextView = view.findViewById(R.id.totalEmployeesReportTextView);
        activeEmployeesReportTextView = view.findViewById(R.id.activeEmployeesReportTextView);
        avgCompletionTimeTextView = view.findViewById(R.id.avgCompletionTimeTextView);
        productivityScoreTextView = view.findViewById(R.id.productivityScoreTextView);
        weeklyCard = view.findViewById(R.id.weeklyCard);
        monthlyCard = view.findViewById(R.id.monthlyCard);
        yearlyCard = view.findViewById(R.id.yearlyCard);
        exportButton = view.findViewById(R.id.exportButton);
        progressBar = view.findViewById(R.id.progressBar);

        String xmlPath = requireContext().getFilesDir() + "/tasks_data.xml";
        xmlDataManager = new XMLDataManager(xmlPath);

        updateDateDisplay();
    }

    private void setupListeners() {
        weeklyCard.setOnClickListener(v -> selectPeriod("WEEKLY"));
        monthlyCard.setOnClickListener(v -> selectPeriod("MONTHLY"));
        yearlyCard.setOnClickListener(v -> selectPeriod("YEARLY"));
        exportButton.setOnClickListener(v -> exportReport());
    }

    private void selectPeriod(String period) {
        selectedPeriod = period;

        weeklyCard.setCardElevation(period.equals("WEEKLY") ? 8f : 4f);
        monthlyCard.setCardElevation(period.equals("MONTHLY") ? 8f : 4f);
        yearlyCard.setCardElevation(period.equals("YEARLY") ? 8f : 4f);

        loadReport();
    }

    private void loadReport() {
        showProgress(true);

        new Thread(() -> {
            try {
                // Charger les statistiques selon la période
                int totalTasks = xmlDataManager.getTaskCountByPeriod(selectedPeriod);
                int completedTasks = xmlDataManager.getCompletedTaskCountByPeriod(selectedPeriod);
                int pendingTasks = xmlDataManager.getPendingTaskCountByPeriod(selectedPeriod);
                int cancelledTasks = xmlDataManager.getCancelledTaskCountByPeriod(selectedPeriod);

                int totalEmployees = xmlDataManager.getTotalEmployeesCount();
                int activeEmployees = xmlDataManager.getActiveEmployeesCount();

                double avgCompletionTime = xmlDataManager.getAverageCompletionTime(selectedPeriod);
                double productivityScore = calculateProductivityScore(completedTasks, totalTasks);

                requireActivity().runOnUiThread(() -> {
                    totalTasksReportTextView.setText(String.valueOf(totalTasks));
                    completedTasksReportTextView.setText(String.valueOf(completedTasks));
                    pendingTasksReportTextView.setText(String.valueOf(pendingTasks));
                    cancelledTasksReportTextView.setText(String.valueOf(cancelledTasks));

                    totalEmployeesReportTextView.setText(String.valueOf(totalEmployees));
                    activeEmployeesReportTextView.setText(String.valueOf(activeEmployees));

                    avgCompletionTimeTextView.setText(String.format("%.1f jours", avgCompletionTime));
                    productivityScoreTextView.setText(String.format("%.1f%%", productivityScore));

                    showProgress(false);
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    showProgress(false);
                });
            }
        }).start();
    }

    private double calculateProductivityScore(int completed, int total) {
        if (total == 0) return 0.0;
        return (double) completed / total * 100;
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE);
        reportDateTextView.setText("Rapport du " + sdf.format(new Date()));
    }

    private void exportReport() {
        // Implémenter l'export PDF ou CSV
        android.widget.Toast.makeText(getContext(),
                "Fonctionnalité d'export en développement",
                android.widget.Toast.LENGTH_SHORT).show();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}