package com.iscms.service;

import com.iscms.dao.MemberDAOImpl;
import com.iscms.dao.MembershipDAOImpl;
import com.iscms.dao.PaymentDAOImpl;
import java.util.ArrayList;
import com.iscms.model.Member;
import com.iscms.model.Membership;
import com.iscms.model.Payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for all reporting queries.
 * UI calls this — never DAO directly.
 */
public class ReportService {

    private final MemberDAOImpl     memberDAO     = new MemberDAOImpl();
    private final MembershipDAOImpl membershipDAO = new MembershipDAOImpl();
    private final PaymentDAOImpl    paymentDAO    = new PaymentDAOImpl();

    // ── Members ───────────────────────────────────────────
    public List<Member> getAllMembers() {
        return memberDAO.findAll();
    }

    public List<Member> getActiveMembers() {
        return memberDAO.findByStatus("ACTIVE");
    }

    public List<Member> getPassiveMembers() {
        return memberDAO.findByStatus("PASSIVE");
    }

    public List<Member> getArchivedMembers() {
        return memberDAO.findByStatus("ARCHIVED");
    }

    public List<Member> getMembersWithBmi() {
        return memberDAO.findAll().stream()
                .filter(m -> m.getBmiValue() != null)
                .collect(Collectors.toList());
    }

    // ── Memberships ───────────────────────────────────────
    /** Active membership for a member (for tier lookup). */
    public Optional<Membership> getActiveMembership(int memberId) {
        return membershipDAO.findActiveByMemberId(memberId);
    }

    /** All memberships for a member (history). */
    public List<Membership> getMembershipsForMember(int memberId) {
        return membershipDAO.findAllByMemberId(memberId);
    }

    /** Members whose membership expires within the given number of days. */
    public List<Membership> getExpiringSoon(int days) {
        return membershipDAO.findExpiringSoon(days);
    }

    // ── Payments ──────────────────────────────────────────
    /** All payments for a specific member. */
    public List<Payment> getPaymentsForMember(int memberId) {
        return paymentDAO.findAllByMemberId(memberId);
    }

    /** All payments in the system. */
    public List<Payment> getAllPayments() {
        return paymentDAO.findAll();
    }

    /** Payments with OVERDUE or PENDING status. */
    public List<Payment> getOverduePayments() {
        // FIX-BUG10: overdue.addAll(pending) — DAO gelecekte unmodifiableList döndürürse
        // UnsupportedOperationException fırlatır. Her zaman yeni ArrayList oluştur.
        List<Payment> result = new ArrayList<>(paymentDAO.findByStatus("OVERDUE"));
        result.addAll(paymentDAO.findByStatus("PENDING"));
        return result;
    }

    // ── KVKK ─────────────────────────────────────────────
    /** Member IDs archived before the given cutoff date (for anonymization report). */
    public List<Integer> getAnonymizedMemberIdsBefore(LocalDateTime cutoff) {
        return memberDAO.findArchivedMemberIdsBefore(cutoff);
    }
}
