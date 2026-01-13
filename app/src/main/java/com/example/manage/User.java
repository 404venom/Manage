package com.example.manage;

public class User {
    private String id;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String role; // SUPERVISOR ou EMPLOYEE
    private String department;
    private String joinDate;
    private boolean active;

    public User() {}

    public User(String id, String username, String password, String fullName,
                String email, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.active = true;
    }

    // Getters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public String getDepartment() { return department; }
    public String getJoinDate() { return joinDate; }
    public boolean isActive() { return active; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setRole(String role) { this.role = role; }
    public void setDepartment(String department) { this.department = department; }
    public void setJoinDate(String joinDate) { this.joinDate = joinDate; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isSupervisor() {
        return "SUPERVISOR".equals(role);
    }

    public boolean isEmployee() {
        return "EMPLOYEE".equals(role);
    }
}
