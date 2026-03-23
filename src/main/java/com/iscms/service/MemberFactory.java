package com.iscms.service;

import com.iscms.model.Member;
import com.iscms.model.MemberBuilder;

import java.time.LocalDate;

public class MemberFactory {

    /**
     * Factory Method: Creates a new member with PENDING status.
     * Tier is stored in Membership table — all members start the same
     * regardless of which tier they selected during registration.
     */
    public static Member createPendingMember(String fullName, LocalDate dob,
                                             String gender, String phone,
                                             String email, String password) {
        return new MemberBuilder()
                .fullName(fullName)
                .dateOfBirth(dob)
                .gender(gender)
                .phone(phone)
                .email(email)
                .password(password)
                .status("PENDING")
                .build();
    }

    /**
     * Factory Method: Creates a member directly as ACTIVE
     * (manager-added, no approval flow needed).
     */
    public static Member createActiveMember(String fullName, LocalDate dob,
                                            String gender, String phone,
                                            String email, String password) {
        return new MemberBuilder()
                .fullName(fullName)
                .dateOfBirth(dob)
                .gender(gender)
                .phone(phone)
                .email(email)
                .password(password)
                .status("ACTIVE")
                .build();
    }
}
