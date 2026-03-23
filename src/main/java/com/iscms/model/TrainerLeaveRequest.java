package com.iscms.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TrainerLeaveRequest {
    private int requestId;
    private int trainerId;
    private LocalDate leaveDate;
    private LocalDate leaveStart;
    private LocalDate leaveEnd;
    private String reason;
    private String status;
    private LocalDateTime createdAt;

    public TrainerLeaveRequest() {}

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public int getTrainerId() { return trainerId; }
    public void setTrainerId(int trainerId) { this.trainerId = trainerId; }

    public LocalDate getLeaveDate() { return leaveDate; }
    public void setLeaveDate(LocalDate leaveDate) { this.leaveDate = leaveDate; }

    public LocalDate getLeaveStart() { return leaveStart; }
    public void setLeaveStart(LocalDate leaveStart) { this.leaveStart = leaveStart; }

    public LocalDate getLeaveEnd() { return leaveEnd; }
    public void setLeaveEnd(LocalDate leaveEnd) { this.leaveEnd = leaveEnd; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}