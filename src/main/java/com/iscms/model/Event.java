package com.iscms.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Event {
    private int eventId;
    private String eventName;
    private String category;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private int capacity;
    private double fee;
    private String minTier;     // CLASSIC, GOLD, VIP
    private int earlyAccessHours;
    private String description;
    private String status;      // ACTIVE, CANCELLED
    private int createdBy;      // manager_id
    private LocalDateTime createdAt;

    public Event() {}

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public double getFee() { return fee; }
    public void setFee(double fee) { this.fee = fee; }

    public String getMinTier() { return minTier; }
    public void setMinTier(String minTier) { this.minTier = minTier; }

    public int getEarlyAccessHours() { return earlyAccessHours; }
    public void setEarlyAccessHours(int earlyAccessHours) { this.earlyAccessHours = earlyAccessHours; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}