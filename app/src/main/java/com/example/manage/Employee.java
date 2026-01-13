package com.example.manage;

public class Employee extends User {
    private String supervisorId;
    private int completedTasks;
    private int pendingTasks;
    private int totalTasks;

    public Employee() {
        super();
        setRole("EMPLOYEE");
    }

    public Employee(String id, String username, String password, String fullName,
                    String email, String supervisorId) {
        super(id, username, password, fullName, email, "EMPLOYEE");
        this.supervisorId = supervisorId;
        this.completedTasks = 0;
        this.pendingTasks = 0;
        this.totalTasks = 0;
    }

    // Getters
    public String getSupervisorId() { return supervisorId; }
    public int getCompletedTasks() { return completedTasks; }
    public int getPendingTasks() { return pendingTasks; }
    public int getTotalTasks() { return totalTasks; }

    // Setters
    public void setSupervisorId(String supervisorId) { this.supervisorId = supervisorId; }
    public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }
    public void setPendingTasks(int pendingTasks) { this.pendingTasks = pendingTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

    // Méthodes utilitaires
    public double getCompletionRate() {
        if (totalTasks == 0) return 0.0;
        return (double) completedTasks / totalTasks * 100;
    }

    public void updateTaskCounts(int pending, int completed) {
        this.pendingTasks = pending;
        this.completedTasks = completed;
        this.totalTasks = pending + completed;
    }
}
