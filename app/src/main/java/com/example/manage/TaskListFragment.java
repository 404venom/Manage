package com.example.manage;


import android.content.Intent;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class TaskListFragment extends Fragment {

    private static final String ARG_FILTER = "filter";

    private RecyclerView tasksRecyclerView;
    private TextView emptyTextView;
    private View progressBar;
    private ChipGroup filterChipGroup;
    private Chip chipAll;
    private Chip chipPending;
    private Chip chipInProgress;
    private Chip chipCompleted;
    private Chip chipUrgent;
    private SearchView searchView;
    private FloatingActionButton fabCreateTask;

    private XMLDataManager xmlDataManager;
    private TaskAdapter taskAdapter;
    private List<Task> allTasks;
    private List<Task> filteredTasks;
    private String currentFilter = "ALL";

    public static TaskListFragment newInstance(String filter) {
        TaskListFragment fragment = new TaskListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILTER, filter);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            currentFilter = getArguments().getString(ARG_FILTER, "ALL");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        initializeViews(view);
        setupListeners();
        loadTasks();

        return view;
    }

    private void initializeViews(View view) {
        tasksRecyclerView = view.findViewById(R.id.tasksRecyclerView);
        emptyTextView = view.findViewById(R.id.emptyTextView);
        progressBar = view.findViewById(R.id.progressBar);
        filterChipGroup = view.findViewById(R.id.filterChipGroup);
        chipAll = view.findViewById(R.id.chipAll);
        chipPending = view.findViewById(R.id.chipPending);
        chipInProgress = view.findViewById(R.id.chipInProgress);
        chipCompleted = view.findViewById(R.id.chipCompleted);
        chipUrgent = view.findViewById(R.id.chipUrgent);
        fabCreateTask = view.findViewById(R.id.fabCreateTask);

        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        String xmlPath = requireContext().getFilesDir() + "/tasks_data.xml";
        xmlDataManager = new XMLDataManager(xmlPath);

        allTasks = new ArrayList<>();
        filteredTasks = new ArrayList<>();

        // Sélectionner le filtre initial
        selectFilterChip(currentFilter);
    }

    private void setupListeners() {
        fabCreateTask.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateTaskActivity.class);
            startActivity(intent);
        });

        // Filtres
        chipAll.setOnClickListener(v -> applyFilter("ALL"));
        chipPending.setOnClickListener(v -> applyFilter("PENDING"));
        chipInProgress.setOnClickListener(v -> applyFilter("IN_PROGRESS"));
        chipCompleted.setOnClickListener(v -> applyFilter("COMPLETED"));
        chipUrgent.setOnClickListener(v -> applyFilter("URGENT"));
    }

    private void loadTasks() {
        showProgress(true);

        new Thread(() -> {
            try {
                // Charger toutes les tâches
                allTasks = xmlDataManager.getAllTasks();

                requireActivity().runOnUiThread(() -> {
                    showProgress(false);
                    applyFilter(currentFilter);
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    showProgress(false);
                    showEmptyState(true);
                });
            }
        }).start();
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        selectFilterChip(filter);

        filteredTasks.clear();

        switch (filter) {
            case "ALL":
                filteredTasks.addAll(allTasks);
                break;
            case "PENDING":
                for (Task task : allTasks) {
                    if ("PENDING".equals(task.getStatus())) {
                        filteredTasks.add(task);
                    }
                }
                break;
            case "IN_PROGRESS":
                for (Task task : allTasks) {
                    if ("IN_PROGRESS".equals(task.getStatus())) {
                        filteredTasks.add(task);
                    }
                }
                break;
            case "COMPLETED":
                for (Task task : allTasks) {
                    if ("COMPLETED".equals(task.getStatus())) {
                        filteredTasks.add(task);
                    }
                }
                break;
            case "URGENT":
                for (Task task : allTasks) {
                    if ("URGENT".equals(task.getPriority())) {
                        filteredTasks.add(task);
                    }
                }
                break;
        }

        displayTasks();
    }

    private void selectFilterChip(String filter) {
        chipAll.setChecked(false);
        chipPending.setChecked(false);
        chipInProgress.setChecked(false);
        chipCompleted.setChecked(false);
        chipUrgent.setChecked(false);

        switch (filter) {
            case "ALL":
                chipAll.setChecked(true);
                break;
            case "PENDING":
                chipPending.setChecked(true);
                break;
            case "IN_PROGRESS":
                chipInProgress.setChecked(true);
                break;
            case "COMPLETED":
                chipCompleted.setChecked(true);
                break;
            case "URGENT":
                chipUrgent.setChecked(true);
                break;
        }
    }

    private void displayTasks() {
        if (filteredTasks.isEmpty()) {
            showEmptyState(true);
            return;
        }

        showEmptyState(false);

        taskAdapter = new TaskAdapter(getContext(), filteredTasks, task -> {
            Intent intent = new Intent(getActivity(), TaskDetailActivity.class);
            intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, task.getId());
            startActivity(intent);
        });

        tasksRecyclerView.setAdapter(taskAdapter);
    }

    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            applyFilter(currentFilter);
            return;
        }

        List<Task> searchResults = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (Task task : filteredTasks) {
            if (task.getTitle().toLowerCase().contains(lowerQuery) ||
                    task.getDescription().toLowerCase().contains(lowerQuery) ||
                    task.getId().toLowerCase().contains(lowerQuery)) {
                searchResults.add(task);
            }
        }

        filteredTasks.clear();
        filteredTasks.addAll(searchResults);
        displayTasks();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        tasksRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean show) {
        emptyTextView.setVisibility(show ? View.VISIBLE : View.GONE);
        tasksRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_task_list, menu);

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
            loadTasks();
            return true;
        } else if (item.getItemId() == R.id.action_sort) {
            showSortDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSortDialog() {
        String[] sortOptions = {"Par date (récent)", "Par date (ancien)", "Par priorité", "Par statut"};

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Trier par")
                .setItems(sortOptions, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            sortByDateDescending();
                            break;
                        case 1:
                            sortByDateAscending();
                            break;
                        case 2:
                            sortByPriority();
                            break;
                        case 3:
                            sortByStatus();
                            break;
                    }
                    displayTasks();
                })
                .show();
    }

    private void sortByDateDescending() {
        filteredTasks.sort((t1, t2) -> t2.getCreatedDate().compareTo(t1.getCreatedDate()));
    }

    private void sortByDateAscending() {
        filteredTasks.sort((t1, t2) -> t1.getCreatedDate().compareTo(t2.getCreatedDate()));
    }

    private void sortByPriority() {
        filteredTasks.sort((t1, t2) -> {
            int p1 = getPriorityValue(t1.getPriority());
            int p2 = getPriorityValue(t2.getPriority());
            return Integer.compare(p2, p1); // Décroissant
        });
    }

    private int getPriorityValue(String priority) {
        switch (priority) {
            case "URGENT": return 4;
            case "HIGH": return 3;
            case "MEDIUM": return 2;
            case "LOW": return 1;
            default: return 0;
        }
    }

    private void sortByStatus() {
        filteredTasks.sort((t1, t2) -> t1.getStatus().compareTo(t2.getStatus()));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasks();
    }
}