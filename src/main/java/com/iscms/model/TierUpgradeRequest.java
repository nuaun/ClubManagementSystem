package com.iscms.model;

import java.time.LocalDateTime;

public class TierUpgradeRequest {
    private int requestId;
    private int memberId;
    private int membershipId;
    private String currentTier;
    private String requestedTier;
    private double upgradeFee;
    private String status; // PENDING, APPROVED, FAILED
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public TierUpgradeRequest() {}

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public int getMembershipId() { return membershipId; }
    public void setMembershipId(int membershipId) { this.membershipId = membershipId; }

    public String getCurrentTier() { return currentTier; }
    public void setCurrentTier(String currentTier) { this.currentTier = currentTier; }

    public String getRequestedTier() { return requestedTier; }
    public void setRequestedTier(String requestedTier) { this.requestedTier = requestedTier; }

    public double getUpgradeFee() { return upgradeFee; }
    public void setUpgradeFee(double upgradeFee) { this.upgradeFee = upgradeFee; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}