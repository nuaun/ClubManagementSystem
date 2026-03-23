package com.iscms.model;

import java.time.LocalDateTime;

public class Manager {
    private int managerId;
    private String fullName;
    private String username;
    private String email;
    private String password;
    private int failedAttempts;
    private boolean isLocked;
    private LocalDateTime createdAt;

    public Manager() {}

    public int getManagerId() { return managerId; }
    public void setManagerId(int managerId) { this.managerId = managerId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    private String role; // ADMIN, MANAGER

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}