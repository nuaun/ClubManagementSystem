package com.iscms.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class EventBuilder {
    private final Event event = new Event();

    public EventBuilder eventName(String name) {
        event.setEventName(name); return this;
    }
    public EventBuilder category(String category) {
        event.setCategory(category); return this;
    }
    public EventBuilder date(LocalDate date) {
        event.setEventDate(date); return this;
    }
    public EventBuilder startTime(LocalTime startTime) {
        event.setStartTime(startTime); return this;
    }
    public EventBuilder endTime(LocalTime endTime) {
        event.setEndTime(endTime); return this;
    }
    public EventBuilder location(String location) {
        event.setLocation(location); return this;
    }
    public EventBuilder capacity(int capacity) {
        event.setCapacity(capacity); return this;
    }
    public EventBuilder fee(double fee) {
        event.setFee(fee); return this;
    }
    public EventBuilder minTier(String minTier) {
        event.setMinTier(minTier); return this;
    }
    public EventBuilder earlyAccessHours(int hours) {
        event.setEarlyAccessHours(hours); return this;
    }
    public EventBuilder description(String description) {
        event.setDescription(description); return this;
    }
    public EventBuilder createdBy(int managerId) {
        event.setCreatedBy(managerId); return this;
    }
    public Event build() {
        // FIX-BUG1: startTime/endTime null-check ÖNCE gelmeli — yoksa capacity=0 testi
        // yanlış exception (name/date) alır. Sıra: zaman → isim/tarih → kapasite.
        if (event.getStartTime() == null || event.getEndTime() == null)
            throw new IllegalStateException("Start time and end time are required.");
        if (event.getEventName() == null || event.getEventDate() == null)
            throw new IllegalStateException("Event name and date are required.");
        if (event.getCapacity() <= 0)
            throw new IllegalStateException("Capacity must be greater than 0.");
        return event;
    }
}