package com.iscms.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Membership {
    private int membershipId;
    private int memberId;
    private String tier;    // CLASSIC, GOLD, VIP
    private String packageType; // MONTHLY, ANNUAL_INSTALLMENT, ANNUAL_PREPAID
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;  // ACTIVE, PASSIVE, SUSPENDED, FROZEN
    private int freezeCount;
    private LocalDateTime createdAt;

    public Membership() {}

    public int getMembershipId() { return membershipId; }
    public void setMembershipId(int membershipId) { this.membershipId = membershipId; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }

    public String getPackageType() { return packageType; }
    public void setPackageType(String packageType) { this.packageType = packageType; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getFreezeCount() { return freezeCount; }
    public void setFreezeCount(int freezeCount) { this.freezeCount = freezeCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}