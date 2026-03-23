package com.iscms.service;

import com.iscms.dao.EventDAOImpl;
import com.iscms.dao.EventRegistrationDAO;
import com.iscms.dao.EventRegistrationDAOImpl;
import com.iscms.model.Event;
import com.iscms.model.EventRegistration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class EventService {

    private final EventDAOImpl eventDAO;
    private final EventRegistrationDAOImpl registrationDAO;

    /** Production constructor. */
    public EventService() {
        this.eventDAO        = new EventDAOImpl();
        this.registrationDAO = new EventRegistrationDAOImpl();
    }

    /** Test constructor — public so test package can access. */
    public EventService(EventDAOImpl eventDAO, EventRegistrationDAOImpl registrationDAO) {
        this.eventDAO        = eventDAO;
        this.registrationDAO = registrationDAO;
    }

    public void createEvent(Event event) {
        if (!event.getEventDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Event date must be at least tomorrow.");
        }
        if (!event.getEndTime().isAfter(event.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time.");
        }
        if (event.getCapacity() <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0.");
        }
        event.setStatus("ACTIVE");
        eventDAO.insert(event);
    }

    public void cancelEvent(int eventId) {
        eventDAO.updateStatus(eventId, "CANCELLED");
        registrationDAO.cancelAllByEventId(eventId);
    }

    public String registerMember(int memberId, int eventId, String memberTier, String memberStatus) {
        if (!"ACTIVE".equals(memberStatus)) {
            throw new IllegalStateException("Only active members can register for events.");
        }

        Event event = eventDAO.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found."));

        if ("CANCELLED".equals(event.getStatus())) {
            throw new IllegalStateException("This event has been cancelled.");
        }

        if (tierRank(memberTier) < tierRank(event.getMinTier())) {
            throw new IllegalStateException("This event requires at least " + event.getMinTier() + " tier.");
        }

        // duplicate check
        if (registrationDAO.existsByMemberAndEvent(memberId, eventId)) {
            throw new IllegalStateException("You are already registered for this event.");
        }

// Aynı tarih ve çakışan saatte başka etkinlik kontrolü
        List<EventRegistration> myRegs = registrationDAO.findByMemberId(memberId);
        for (EventRegistration reg : myRegs) {
            if ("CANCELLED".equals(reg.getPaymentStatus())) continue;
            Optional<Event> existingOpt = eventDAO.findById(reg.getEventId());
            if (existingOpt.isEmpty()) continue;
            Event existing = existingOpt.get();
            if (!existing.getEventDate().equals(event.getEventDate())) continue;
            if ("CANCELLED".equals(existing.getStatus())) continue;
            boolean overlap = existing.getStartTime().isBefore(event.getEndTime())
                    && existing.getEndTime().isAfter(event.getStartTime());
            if (overlap) {
                throw new IllegalStateException(
                        "You already have a registration that overlaps with this event: "
                                + existing.getEventName() + " (" + existing.getStartTime()
                                + " - " + existing.getEndTime() + ")");
            }
        }



        if ("CLASSIC".equals(memberTier)) {
            LocalDateTime earliest = event.getCreatedAt().plusHours(event.getEarlyAccessHours());
            if (LocalDateTime.now().isBefore(earliest)) {
                throw new IllegalStateException(
                        "Classic members can register after " + earliest.toLocalDate() + " " + earliest.toLocalTime());
            }
        }

        int registered = registrationDAO.countRegistered(eventId);
        if (registered >= event.getCapacity()) {
            if ("CLASSIC".equals(memberTier)) {
                throw new IllegalStateException("Event is full.");
            }
            int waitPos = registrationDAO.getMaxWaitlistPosition(eventId) + 1;
            EventRegistration reg = new EventRegistration();
            reg.setMemberId(memberId);
            reg.setEventId(eventId);
            reg.setWaitlistPosition(waitPos);
            registrationDAO.insert(reg);
            return "You have been added to the waitlist at position " + waitPos + ".";
        }

        EventRegistration reg = new EventRegistration();
        reg.setMemberId(memberId);
        reg.setEventId(eventId);
        registrationDAO.insert(reg);
        return null;
    }

    public void cancelRegistration(int memberId, int eventId) {
        Event event = eventDAO.findById(eventId).orElseThrow();
        LocalDateTime deadline = event.getEventDate().atTime(event.getStartTime()).minusHours(24);
        if (LocalDateTime.now().isAfter(deadline)) {
            throw new IllegalStateException("Cannot cancel within 24 hours of the event.");
        }
        registrationDAO.deleteByMemberAndEvent(memberId, eventId);

        registrationDAO.findFirstWaitlist(eventId).ifPresent(waiting -> {
            registrationDAO.promoteFromWaitlist(waiting.getRegistrationId());
            registrationDAO.reorderWaitlistAfterPromotion(eventId);
        });
    }

    public List<Event> getActiveEvents()          { return eventDAO.findActiveEvents(); }
    public List<Event> getAllEvents()              { return eventDAO.findAll(); }
    public Optional<Event> getEventById(int id)   { return eventDAO.findById(id); }
    public void updateEvent(Event event)           { eventDAO.update(event); }

    public int countRegistered(int eventId) {
        return registrationDAO.countRegistered(eventId);
    }

    public List<EventRegistration> getRegistrationsByEvent(int eventId) {
        return registrationDAO.findByEventId(eventId);
    }

    public List<EventRegistration> getRegistrationsByMember(int memberId) {
        return registrationDAO.findByMemberId(memberId);
    }

    private int tierRank(String tier) {
        return switch (tier) {
            case "CLASSIC" -> 1;
            case "GOLD"    -> 2;
            case "VIP"     -> 3;
            default        -> 0;
        };
    }
}