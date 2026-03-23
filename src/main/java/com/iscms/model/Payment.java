package com.iscms.model;

import java.time.LocalDateTime;

public class Payment {
    private int paymentId;
    private int memberId;
    private double amount;
    private LocalDateTime paymentDate;
    private String paymentType; // MEMBERSHIP, INSTALLMENT, EVENT, UPGRADE, MANUAL_CASH
    private String description;
    private String status;      // PAID, PENDING, OVERDUE
    private int recordedBy;     // manager_id

    public Payment() {}

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getRecordedBy() { return recordedBy; }
    public void setRecordedBy(int recordedBy) { this.recordedBy = recordedBy; }
}