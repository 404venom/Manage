package com.example.manage;


public class Notification {
    private String id;
    private String userId;
    private String taskId;
    private String type; // TASK_ASSIGNED, TASK_UPDATED, TASK_COMPLETED, REMINDER
    private String title;
    private String message;
    private String timestamp;
    private boolean read;

    public Notification() {
        this.read = false;
    }

    public Notification(String id, String userId, String taskId, String type,
                        String title, String message, String timestamp) {
        this.id = id;
        this.userId = userId;
        this.taskId = taskId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.read = false;
    }

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getTaskId() { return taskId; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }
    public boolean isRead() { return read; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public void setType(String type) { this.type = type; }
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public void setRead(boolean read) { this.read = read; }

    // Méthodes utilitaires
    public String getFormattedTime() {
        // Convertir le timestamp en format lisible
        // Format: YYYY-MM-DDTHH:MM:SS -> format relatif
        if (timestamp == null) return "";

        // À implémenter: calcul du temps relatif (il y a 2h, hier, etc.)
        return timestamp;
    }

    public int getIconResource() {
        // Retourner l'icône appropriée selon le type
        switch (type) {
            case "TASK_ASSIGNED":
                return R.drawable.ic_task_assigned;
            case "TASK_UPDATED":
                return R.drawable.ic_task_updated;
            case "TASK_COMPLETED":
                return R.drawable.ic_task_completed;
            case "REMINDER":
                return R.drawable.ic_reminder;
            default:
                return R.drawable.ic_notification;
        }
    }
}
