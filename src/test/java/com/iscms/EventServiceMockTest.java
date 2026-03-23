package com.iscms;

import com.iscms.dao.EventDAOImpl;
import com.iscms.dao.EventRegistrationDAOImpl;
import com.iscms.model.Event;
import com.iscms.model.EventBuilder;
import com.iscms.service.EventService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceMockTest {

    @Mock private EventDAOImpl             eventDAO;
    @Mock private EventRegistrationDAOImpl registrationDAO;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventService(eventDAO, registrationDAO);
    }

    // ── createEvent ───────────────────────────────────────────────

    @Test
    void createEvent_pastDate_throwsAndNeverInserts() {
        Event e = event(LocalDate.now().minusDays(1));
        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(e));
        verify(eventDAO, never()).insert(any());
    }

    @Test
    void createEvent_today_throwsAndNeverInserts() {
        Event e = event(LocalDate.now());
        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(e));
        verify(eventDAO, never()).insert(any());
    }

    @Test
    void createEvent_endTimeBeforeStart_throwsIllegalArgument() {
        Event e = event(LocalDate.now().plusDays(3));
        e.setStartTime(LocalTime.of(12, 0));
        e.setEndTime(LocalTime.of(10, 0));
        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(e));
    }

    @Test
    void createEvent_zeroCapacity_throwsIllegalArgument() {
        Event e = event(LocalDate.now().plusDays(3));
        e.setCapacity(0);
        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(e));
    }

    @Test
    void createEvent_validFutureEvent_setsActiveAndInserts() {
        Event e = event(LocalDate.now().plusDays(5));
        eventService.createEvent(e);
        assertEquals("ACTIVE", e.getStatus());
        verify(eventDAO).insert(e);
    }

    // ── cancelEvent ───────────────────────────────────────────────

    @Test
    void cancelEvent_marksStatusAndCancelsRegistrations() {
        eventService.cancelEvent(99);
        verify(eventDAO).updateStatus(99, "CANCELLED");
        verify(registrationDAO).cancelAllByEventId(99);
    }

    // ── registerMember ────────────────────────────────────────────

    @Test
    void registerMember_passiveMember_throwsIllegalState() {
        assertThrows(IllegalStateException.class, () ->
                eventService.registerMember(1, 10, "GOLD", "PASSIVE"));
    }

    @Test
    void registerMember_cancelledEvent_throwsIllegalState() {
        Event e = event(LocalDate.now().plusDays(3));
        e.setEventId(10);
        e.setStatus("CANCELLED");
        when(eventDAO.findById(10)).thenReturn(Optional.of(e));

        assertThrows(IllegalStateException.class, () ->
                eventService.registerMember(1, 10, "GOLD", "ACTIVE"));
    }

    @Test
    void registerMember_tierTooLow_throwsIllegalState() {
        Event e = event(LocalDate.now().plusDays(5));
        e.setEventId(10);
        e.setStatus("ACTIVE");
        e.setMinTier("VIP");
        e.setCreatedAt(LocalDateTime.now().minusDays(2));
        when(eventDAO.findById(10)).thenReturn(Optional.of(e));
        when(registrationDAO.existsByMemberAndEvent(anyInt(), anyInt())).thenReturn(false);

        assertThrows(IllegalStateException.class, () ->
                eventService.registerMember(1, 10, "CLASSIC", "ACTIVE"));
    }

    @Test
    void registerMember_alreadyRegistered_throwsIllegalState() {
        Event e = event(LocalDate.now().plusDays(5));
        e.setEventId(10);
        e.setStatus("ACTIVE");
        e.setMinTier("CLASSIC");
        e.setCreatedAt(LocalDateTime.now().minusDays(2));
        when(eventDAO.findById(10)).thenReturn(Optional.of(e));
        when(registrationDAO.existsByMemberAndEvent(1, 10)).thenReturn(true);

        assertThrows(IllegalStateException.class, () ->
                eventService.registerMember(1, 10, "GOLD", "ACTIVE"));
    }

    @Test
    void registerMember_classicAndFull_throwsIllegalState() {
        Event e = event(LocalDate.now().plusDays(5));
        e.setEventId(20);
        e.setStatus("ACTIVE");
        e.setMinTier("CLASSIC");
        e.setCapacity(1);
        e.setCreatedAt(LocalDateTime.now().minusDays(2));
        when(eventDAO.findById(20)).thenReturn(Optional.of(e));
        when(registrationDAO.existsByMemberAndEvent(anyInt(), anyInt())).thenReturn(false);
        when(registrationDAO.countRegistered(20)).thenReturn(1);

        assertThrows(IllegalStateException.class, () ->
                eventService.registerMember(5, 20, "CLASSIC", "ACTIVE"));
    }

    @Test
    void registerMember_goldAndFull_addsToWaitlist() {
        Event e = event(LocalDate.now().plusDays(5));
        e.setEventId(21);
        e.setStatus("ACTIVE");
        e.setMinTier("CLASSIC");
        e.setCapacity(1);
        e.setCreatedAt(LocalDateTime.now().minusDays(2));
        when(eventDAO.findById(21)).thenReturn(Optional.of(e));
        when(registrationDAO.existsByMemberAndEvent(anyInt(), anyInt())).thenReturn(false);
        when(registrationDAO.countRegistered(21)).thenReturn(1);
        when(registrationDAO.getMaxWaitlistPosition(21)).thenReturn(0);

        String result = eventService.registerMember(5, 21, "GOLD", "ACTIVE");

        assertNotNull(result);
        verify(registrationDAO).insert(any());
    }

    // ── cancelRegistration ────────────────────────────────────────

    @Test
    void cancelRegistration_callsDao() {
        Event e = event(LocalDate.now().plusDays(3));
        e.setEventId(10);
        when(eventDAO.findById(10)).thenReturn(Optional.of(e));

        eventService.cancelRegistration(1, 10);

        verify(registrationDAO).deleteByMemberAndEvent(1, 10);
    }

    // ── Helper ───────────────────────────────────────────────────

    private Event event(LocalDate date) {
        return new EventBuilder()
                .eventName("Test Event")
                .category("YOGA")
                .date(date)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .location("Studio A")
                .capacity(20)
                .fee(0.0)
                .minTier("CLASSIC")
                .earlyAccessHours(24)
                .createdBy(1)
                .build();
    }
}