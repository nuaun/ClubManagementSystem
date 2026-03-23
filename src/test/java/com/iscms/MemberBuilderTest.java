package com.iscms;

import com.iscms.model.Member;
import com.iscms.model.MemberBuilder;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;

public class MemberBuilderTest {

    @Test
    void testBuilder_validMember() {
        Member m = new MemberBuilder()
                .fullName("Ali Veli")
                .dateOfBirth(LocalDate.of(1995, 5, 10))
                .gender("MALE")
                .phone("5551234567")
                .email("ali@test.com")
                .password("pass123")
                .weight(75.0)
                .height(180.0)
                .status("ACTIVE")
                .build();

        assertEquals("Ali Veli", m.getFullName());
        assertEquals("5551234567", m.getPhone());
        assertEquals("ACTIVE", m.getStatus());
        assertEquals(75.0, m.getWeight());
    }

    @Test
    void testBuilder_missingName_throwsException() {
        assertThrows(IllegalStateException.class, () ->
                new MemberBuilder()
                        .phone("5551234567")
                        .email("test@test.com")
                        .password("pass123")
                        .build()
        );
    }

    @Test
    void testBuilder_missingPhone_throwsException() {
        assertThrows(IllegalStateException.class, () ->
                new MemberBuilder()
                        .fullName("Ali Veli")
                        .email("test@test.com")
                        .password("pass123")
                        .build()
        );
    }

    // FIX-7: email null kontrolü — eklenen test
    @Test
    void testBuilder_missingEmail_throwsException() {
        assertThrows(IllegalStateException.class, () ->
                new MemberBuilder()
                        .fullName("Ali Veli")
                        .phone("5551234567")
                        .password("pass123")
                        .build()
        );
    }

    // FIX-7: password null kontrolü — eklenen test
    @Test
    void testBuilder_missingPassword_throwsException() {
        assertThrows(IllegalStateException.class, () ->
                new MemberBuilder()
                        .fullName("Ali Veli")
                        .phone("5551234567")
                        .email("test@test.com")
                        .build()
        );
    }
}