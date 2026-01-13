package com.example.manage;

public class Task {
    private String id;
    private String title;
    private String description;
    private String priority;
    private String status;
    private String assignedTo;
    private String createdBy;
    private String dueDate;
    private String createdDate;

    // Constructeurs, getters et setters
    public Task() {}

    public Task(String id, String title, String description, String priority,
                String status, String assignedTo, String createdBy,
                String dueDate, String createdDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.assignedTo = assignedTo;
        this.createdBy = createdBy;
        this.dueDate = dueDate;
        this.createdDate = createdDate;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPriority() { return priority; }
    public String getStatus() { return status; }
    public String getAssignedTo() { return assignedTo; }
    public String getCreatedBy() { return createdBy; }
    public String getDueDate() { return dueDate; }
    public String getCreatedDate() { return createdDate; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPriority(String priority) { this.priority = priority; }
    public void setStatus(String status) { this.status = status; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
}