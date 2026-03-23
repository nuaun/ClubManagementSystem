package com.iscms.model;

import java.time.LocalDate;

public class MemberBuilder {
    private final Member member = new Member();

    public MemberBuilder fullName(String fullName) {
        member.setFullName(fullName); return this;
    }
    public MemberBuilder dateOfBirth(LocalDate dob) {
        member.setDateOfBirth(dob); return this;
    }
    public MemberBuilder gender(String gender) {
        member.setGender(gender); return this;
    }
    public MemberBuilder phone(String phone) {
        member.setPhone(phone); return this;
    }
    public MemberBuilder email(String email) {
        member.setEmail(email); return this;
    }
    public MemberBuilder password(String password) {
        member.setPassword(password); return this;
    }
    public MemberBuilder weight(Double weight) {
        member.setWeight(weight); return this;
    }
    public MemberBuilder height(Double height) {
        member.setHeight(height); return this;
    }
    public MemberBuilder emergencyContact(String name, String phone) {
        member.setEmergencyContactName(name);
        member.setEmergencyContactPhone(phone);
        return this;
    }
    public MemberBuilder status(String status) {
        member.setStatus(status); return this;
    }
    public Member build() {
        // FIX-7: email ve password zorunlu alan — null bırakılmamalı
        if (member.getFullName() == null || member.getFullName().isBlank())
            throw new IllegalStateException("Full name is required.");
        if (member.getPhone() == null || member.getPhone().isBlank())
            throw new IllegalStateException("Phone is required.");
        if (member.getEmail() == null || member.getEmail().isBlank())
            throw new IllegalStateException("Email is required.");
        if (member.getPassword() == null || member.getPassword().isBlank())
            throw new IllegalStateException("Password is required.");
        return member;
    }
}