package com.iscms;

import com.iscms.model.Event;
import com.iscms.model.EventBuilder;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.time.LocalTime;

// BUG-1 FIX: startTime/endTime are required fields checked before capacity and name in EventBuilder.build().
// Tests that omit them would trigger "Start time and end time are required." instead of the intended exception.

public class EventBuilderTest {

    @Test
    void testEventBuilder_validEvent() {
        Event e = new EventBuilder()
                .eventName("Morning Yoga")
                .category("YOGA")
                .date(LocalDate.now().plusDays(5))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .location("Studio A")
                .capacity(20)
                .fee(0.0)
                .minTier("GOLD")
                .earlyAccessHours(24)
                .createdBy(1)
                .build();

        assertEquals("Morning Yoga", e.getEventName());
        assertEquals("YOGA", e.getCategory());
        assertEquals(20, e.getCapacity());
        assertEquals("GOLD", e.getMinTier());
    }

    @Test
    void testEventBuilder_zeroCapacity_throwsException() {
        // BUG-1 FIX: startTime/endTime must be set, otherwise their null-check fires first
        assertThrows(IllegalStateException.class, () ->
                new EventBuilder()
                        .eventName("Test")
                        .date(LocalDate.now().plusDays(1))
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(10, 0))
                        .capacity(0)
                        .build()
        );
    }

    @Test
    void testEventBuilder_missingName_throwsException() {
        // BUG-1 FIX: startTime/endTime must be set, otherwise their null-check fires before name-check
        assertThrows(IllegalStateException.class, () ->
                new EventBuilder()
                        .date(LocalDate.now().plusDays(1))
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(10, 0))
                        .capacity(10)
                        .build()
        );
    }
}