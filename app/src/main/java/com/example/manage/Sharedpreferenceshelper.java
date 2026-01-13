package com.example.manage;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;

public class SharedPreferencesHelper {

    private static final String PREF_NAME = "TaskManagerPrefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_DATA = "user_data";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Gson gson;

    public SharedPreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        gson = new Gson();
    }

    /**
     * Enregistrer la session de l'utilisateur
     */
    public void saveUserSession(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);

        // Sérialiser l'objet User en JSON
        String userJson = gson.toJson(user);
        editor.putString(KEY_USER_DATA, userJson);

        editor.apply();
    }

    /**
     * Récupérer la session de l'utilisateur
     */
    public User getUserSession() {
        if (!isLoggedIn()) {
            return null;
        }

        String userJson = sharedPreferences.getString(KEY_USER_DATA, null);
        if (userJson != null) {
            return gson.fromJson(userJson, User.class);
        }

        return null;
    }

    /**
     * Vérifier si l'utilisateur est connecté
     */
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Effacer la session de l'utilisateur (déconnexion)
     */
    public void clearUserSession() {
        editor.clear();
        editor.apply();
    }

    /**
     * Mettre à jour les informations de l'utilisateur
     */
    public void updateUserData(User user) {
        String userJson = gson.toJson(user);
        editor.putString(KEY_USER_DATA, userJson);
        editor.apply();
    }

    /**
     * Obtenir l'ID de l'utilisateur connecté
     */
    public String getCurrentUserId() {
        User user = getUserSession();
        return user != null ? user.getId() : null;
    }

    /**
     * Obtenir le rôle de l'utilisateur connecté
     */
    public String getCurrentUserRole() {
        User user = getUserSession();
        return user != null ? user.getRole() : null;
    }
}