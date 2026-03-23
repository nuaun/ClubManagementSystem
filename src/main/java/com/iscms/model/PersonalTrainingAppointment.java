package com.iscms.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class PersonalTrainingAppointment {
    private int appointmentId;
    private int memberId;
    private int trainerId;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status; // SCHEDULED, COMPLETED, CANCELLED, NO_SHOW
    private LocalDate noShowPenaltyUntil;
    private LocalDateTime createdAt;

    public PersonalTrainingAppointment() {}

    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public int getTrainerId() { return trainerId; }
    public void setTrainerId(int trainerId) { this.trainerId = trainerId; }

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getNoShowPenaltyUntil() { return noShowPenaltyUntil; }
    public void setNoShowPenaltyUntil(LocalDate noShowPenaltyUntil) { this.noShowPenaltyUntil = noShowPenaltyUntil; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}