package com.example.manage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class EmployeeListFragment extends Fragment {

    private RecyclerView employeesRecyclerView;
    private TextView emptyTextView;
    private View progressBar;
    private TextView totalEmployeesTextView;
    private TextView activeEmployeesTextView;
    private SearchView searchView;

    private XMLDataManager xmlDataManager;
    private EmployeeAdapter employeeAdapter;
    private List<Employee> allEmployees;
    private List<Employee> filteredEmployees;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_employee_list, container, false);

        initializeViews(view);
        loadEmployees();

        return view;
    }

    private void initializeViews(View view) {
        employeesRecyclerView = view.findViewById(R.id.employeesRecyclerView);
        emptyTextView = view.findViewById(R.id.emptyTextView);
        progressBar = view.findViewById(R.id.progressBar);
        totalEmployeesTextView = view.findViewById(R.id.totalEmployeesTextView);
        activeEmployeesTextView = view.findViewById(R.id.activeEmployeesTextView);

        // Grid Layout avec 2 colonnes
        employeesRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        String xmlPath = requireContext().getFilesDir() + "/users_data.xml";
        xmlDataManager = new XMLDataManager(xmlPath);

        allEmployees = new ArrayList<>();
        filteredEmployees = new ArrayList<>();
    }

    private void loadEmployees() {
        showProgress(true);

        new Thread(() -> {
            try {
                // Charger tous les employés
                allEmployees = xmlDataManager.getAllEmployees();
                filteredEmployees = new ArrayList<>(allEmployees);

                // Calculer les statistiques
                int totalEmployees = allEmployees.size();
                int activeEmployees = 0;

                for (Employee employee : allEmployees) {
                    if (employee.isActive()) {
                        activeEmployees++;
                    }
                }

                final int finalActiveCount = activeEmployees;

                requireActivity().runOnUiThread(() -> {
                    showProgress(false);

                    totalEmployeesTextView.setText(String.valueOf(totalEmployees));
                    activeEmployeesTextView.setText(String.valueOf(finalActiveCount));

                    displayEmployees();
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    showProgress(false);
                    showEmptyState(true);
                });
            }
        }).start();
    }

    private void displayEmployees() {
        if (filteredEmployees.isEmpty()) {
            showEmptyState(true);
            return;
        }

        showEmptyState(false);

        employeeAdapter = new EmployeeAdapter(getContext(), filteredEmployees, employee -> {
            showEmployeeDetailsDialog(employee);
        });

        employeesRecyclerView.setAdapter(employeeAdapter);
    }

    private void showEmployeeDetailsDialog(Employee employee) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_employee_details, null);

        TextView nameTextView = dialogView.findViewById(R.id.nameTextView);
        TextView emailTextView = dialogView.findViewById(R.id.emailTextView);
        TextView phoneTextView = dialogView.findViewById(R.id.phoneTextView);
        TextView departmentTextView = dialogView.findViewById(R.id.departmentTextView);
        TextView totalTasksTextView = dialogView.findViewById(R.id.totalTasksTextView);
        TextView completedTasksTextView = dialogView.findViewById(R.id.completedTasksTextView);
        TextView pendingTasksTextView = dialogView.findViewById(R.id.pendingTasksTextView);
        TextView completionRateTextView = dialogView.findViewById(R.id.completionRateTextView);

        nameTextView.setText(employee.getFullName());
        emailTextView.setText(employee.getEmail());
        phoneTextView.setText(employee.getPhone() != null ? employee.getPhone() : "Non renseigné");
        departmentTextView.setText(employee.getDepartment() != null ? employee.getDepartment() : "Non renseigné");

        // Charger les statistiques des tâches
        new Thread(() -> {
            int totalTasks = xmlDataManager.getTaskCountByEmployee(employee.getId());
            int completedTasks = xmlDataManager.getCompletedTaskCountByEmployee(employee.getId());
            int pendingTasks = totalTasks - completedTasks;
            double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0.0;

            requireActivity().runOnUiThread(() -> {
                totalTasksTextView.setText(String.valueOf(totalTasks));
                completedTasksTextView.setText(String.valueOf(completedTasks));
                pendingTasksTextView.setText(String.valueOf(pendingTasks));
                completionRateTextView.setText(String.format("%.1f%%", completionRate));
            });
        }).start();

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Détails de l'employé")
                .setView(dialogView)
                .setPositiveButton("Assigner une tâche", (dialog, which) -> {
                    openAssignTaskFragment(employee);
                })
                .setNegativeButton("Fermer", null)
                .show();
    }

    private void openAssignTaskFragment(Employee employee) {
        AssignTaskFragment fragment = AssignTaskFragment.newInstance(employee.getId());
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            filteredEmployees = new ArrayList<>(allEmployees);
            displayEmployees();
            return;
        }

        List<Employee> searchResults = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (Employee employee : allEmployees) {
            if (employee.getFullName().toLowerCase().contains(lowerQuery) ||
                    employee.getEmail().toLowerCase().contains(lowerQuery) ||
                    (employee.getDepartment() != null && employee.getDepartment().toLowerCase().contains(lowerQuery))) {
                searchResults.add(employee);
            }
        }

        filteredEmployees = searchResults;
        displayEmployees();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        employeesRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean show) {
        emptyTextView.setVisibility(show ? View.VISIBLE : View.GONE);
        employeesRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_employee_list, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            loadEmployees();
            return true;
        } else if (item.getItemId() == R.id.action_sort) {
            showSortDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSortDialog() {
        String[] sortOptions = {"Par nom (A-Z)", "Par nom (Z-A)", "Par département", "Par taux de complétion"};

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Trier par")
                .setItems(sortOptions, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            sortByNameAscending();
                            break;
                        case 1:
                            sortByNameDescending();
                            break;
                        case 2:
                            sortByDepartment();
                            break;
                        case 3:
                            sortByCompletionRate();
                            break;
                    }
                    displayEmployees();
                })
                .show();
    }

    private void sortByNameAscending() {
        filteredEmployees.sort((e1, e2) -> e1.getFullName().compareTo(e2.getFullName()));
    }

    private void sortByNameDescending() {
        filteredEmployees.sort((e1, e2) -> e2.getFullName().compareTo(e1.getFullName()));
    }

    private void sortByDepartment() {
        filteredEmployees.sort((e1, e2) -> {
            String d1 = e1.getDepartment() != null ? e1.getDepartment() : "";
            String d2 = e2.getDepartment() != null ? e2.getDepartment() : "";
            return d1.compareTo(d2);
        });
    }

    private void sortByCompletionRate() {
        filteredEmployees.sort((e1, e2) ->
                Double.compare(e2.getCompletionRate(), e1.getCompletionRate())
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEmployees();
    }
}