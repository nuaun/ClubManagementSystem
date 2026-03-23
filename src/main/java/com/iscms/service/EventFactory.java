package com.iscms.service;

import com.iscms.model.Event;
import com.iscms.model.EventBuilder;

import java.time.LocalDate;
import java.time.LocalTime;

public class EventFactory {

    // Factory Method: Free event
    public static Event createFreeEvent(String name, String category,
                                        LocalDate date, LocalTime start, LocalTime end,
                                        String location, int capacity,
                                        String minTier, int managerId) {
        return new EventBuilder()
                .eventName(name)
                .category(category)
                .date(date)
                .startTime(start)
                .endTime(end)
                .location(location)
                .capacity(capacity)
                .fee(0.0)
                .minTier(minTier)
                .earlyAccessHours(24)
                .createdBy(managerId)
                .build();
    }

    // Factory Method: Paid event
    public static Event createPaidEvent(String name, String category,
                                        LocalDate date, LocalTime start, LocalTime end,
                                        String location, int capacity, double fee,
                                        String minTier, int managerId) {
        return new EventBuilder()
                .eventName(name)
                .category(category)
                .date(date)
                .startTime(start)
                .endTime(end)
                .location(location)
                .capacity(capacity)
                .fee(fee)
                .minTier(minTier)
                .earlyAccessHours(24)
                .createdBy(managerId)
                .build();
    }

    // Factory Method: VIP-only event
    public static Event createVipEvent(String name,
                                       LocalDate date, LocalTime start, LocalTime end,
                                       String location, int capacity, double fee,
                                       int managerId) {
        return new EventBuilder()
                .eventName(name)
                .category("VIP_ONLY")
                .date(date)
                .startTime(start)
                .endTime(end)
                .location(location)
                .capacity(capacity)
                .fee(fee)
                .minTier("VIP")
                .earlyAccessHours(0)
                .createdBy(managerId)
                .build();
    }
}