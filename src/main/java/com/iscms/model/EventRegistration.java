package com.iscms.model;

import java.time.LocalDateTime;

public class EventRegistration {
    private int registrationId;
    private int memberId;
    private int eventId;
    private LocalDateTime registrationDate;
    private String paymentStatus; // FREE, PAID, PENDING
    private Integer waitlistPosition;

    public EventRegistration() {}

    public int getRegistrationId() { return registrationId; }
    public void setRegistrationId(int registrationId) { this.registrationId = registrationId; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public Integer getWaitlistPosition() { return waitlistPosition; }
    public void setWaitlistPosition(Integer waitlistPosition) { this.waitlistPosition = waitlistPosition; }
}