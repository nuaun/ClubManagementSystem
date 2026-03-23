package com.iscms.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Member {
    private int memberId;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String phone;
    private String email;
    private String password;
    private Double weight;
    private Double height;
    private Double bmiValue;
    private String bmiCategory;
    private LocalDateTime bmiUpdatedAt;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String status; // ACTIVE, PASSIVE, SUSPENDED, ARCHIVED
    private int failedAttempts;
    private boolean isLocked;
    private LocalDateTime archivedAt;
    private LocalDateTime createdAt;

    public Member() {}

    // Getters & Setters
    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }

    public Double getBmiValue() { return bmiValue; }
    public void setBmiValue(Double bmiValue) { this.bmiValue = bmiValue; }

    public String getBmiCategory() { return bmiCategory; }
    public void setBmiCategory(String bmiCategory) { this.bmiCategory = bmiCategory; }

    public LocalDateTime getBmiUpdatedAt() { return bmiUpdatedAt; }
    public void setBmiUpdatedAt(LocalDateTime bmiUpdatedAt) { this.bmiUpdatedAt = bmiUpdatedAt; }

    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }

    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }

    public LocalDateTime getArchivedAt() { return archivedAt; }
    public void setArchivedAt(LocalDateTime archivedAt) { this.archivedAt = archivedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}