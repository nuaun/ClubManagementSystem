package com.iscms.model;

import java.time.LocalDateTime;

public class RegistrationRequest {
    private int requestId;
    private int memberId;
    private String tier;
    private String packageType;
    private double amount;
    private String status; // PENDING, APPROVED, FAILED
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public RegistrationRequest() {}

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }

    public String getPackageType() { return packageType; }
    public void setPackageType(String packageType) { this.packageType = packageType; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}