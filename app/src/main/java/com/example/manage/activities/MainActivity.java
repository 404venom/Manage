package com.example.manage.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.manage.DashboardFragment;
import com.example.manage.EmployeeListFragment;
import com.example.manage.NotificationActivity;
import com.example.manage.ProfileActivity;
import com.example.manage.R;
import com.example.manage.SharedPreferencesHelper;
import com.example.manage.TaskListFragment;
import com.example.manage.User;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView navHeaderName;
    private TextView navHeaderEmail;

    private SharedPreferencesHelper prefsHelper;
    private User currentUser;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupNavigationDrawer();
        loadUserData();
        setupNavigationMenu();
        loadDefaultFragment();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);

        // Navigation Header
        View headerView = navigationView.getHeaderView(0);
        navHeaderName = headerView.findViewById(R.id.navHeaderName);
        navHeaderEmail = headerView.findViewById(R.id.navHeaderEmail);
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void loadUserData() {
        prefsHelper = new SharedPreferencesHelper(this);
        currentUser = prefsHelper.getUserSession();

        if (currentUser == null) {
            // Rediriger vers login si pas de session
            redirectToLogin();
            return;
        }

        userRole = currentUser.getRole();

        // Mettre à jour le header
        navHeaderName.setText(currentUser.getFullName());
        navHeaderEmail.setText(currentUser.getEmail());
    }

    private void setupNavigationMenu() {
        // Afficher/masquer les menus selon le rôle
        if ("SUPERVISOR".equals(userRole)) {
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.menu_supervisor_navigation);
        } else {
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.menu_employee_navigation);
        }
    }

    private void loadDefaultFragment() {
        Fragment fragment;
        String title;

        if ("SUPERVISOR".equals(userRole)) {
            fragment = new DashboardFragment();
            title = "Tableau de Bord";
        } else {
            fragment = new MyTasksFragment();
            title = "Mes Tâches";
        }

        loadFragment(fragment, title);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        String title = "";
        int itemId = item.getItemId();

        if ("SUPERVISOR".equals(userRole)) {
            // Menu Superviseur
            if (itemId == R.id.nav_dashboard) {
                fragment = new DashboardFragment();
                title = "Tableau de Bord";
            } else if (itemId == R.id.nav_all_tasks) {
                fragment = new TaskListFragment();
                title = "Toutes les Tâches";
            } else if (itemId == R.id.nav_employees) {
                fragment = new EmployeeListFragment();
                title = "Employés";
            } else if (itemId == R.id.nav_create_task) {
                startActivity(new Intent(this, CreateTaskActivity.class));
            } else if (itemId == R.id.nav_reports) {
                Toast.makeText(this, "Rapports - En développement", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Menu Employé
            if (itemId == R.id.nav_my_tasks) {
                fragment = new MyTasksFragment();
                title = "Mes Tâches";
            } else if (itemId == R.id.nav_in_progress) {
                fragment = new TaskProgressFragment();
                title = "En Cours";
            } else if (itemId == R.id.nav_completed) {
                fragment = new CompletedTasksFragment();
                title = "Terminées";
            } else if (itemId == R.id.nav_calendar) {
                Toast.makeText(this, "Calendrier - En développement", Toast.LENGTH_SHORT).show();
            }
        }

        // Options communes
        if (itemId == R.id.nav_notifications) {
            startActivity(new Intent(this, NotificationActivity.class));
        } else if (itemId == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (itemId == R.id.nav_settings) {
            Toast.makeText(this, "Paramètres - En développement", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_logout) {
            showLogoutDialog();
        }

        if (fragment != null) {
            loadFragment(fragment, title);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragment(Fragment fragment, String title) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Déconnexion")
                .setMessage("Voulez-vous vraiment vous déconnecter ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    prefsHelper.clearUserSession();
                    redirectToLogin();
                    Toast.makeText(this, "Déconnexion réussie", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // Confirmer avant de quitter
            new AlertDialog.Builder(this)
                    .setTitle("Quitter l'application")
                    .setMessage("Voulez-vous quitter l'application ?")
                    .setPositiveButton("Oui", (dialog, which) -> finish())
                    .setNegativeButton("Non", null)
                    .show();
        }
    }
}