package com.iscms;

import com.iscms.dao.MemberDAOImpl;
import com.iscms.dao.MembershipDAOImpl;
import com.iscms.dao.PaymentDAOImpl;
import com.iscms.dao.RequestDAOImpl;
import com.iscms.model.Member;
import com.iscms.model.MemberBuilder;
import com.iscms.model.Membership;
import com.iscms.model.RegistrationRequest;
import com.iscms.service.MemberService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberServiceMockTest {

    @Mock private MemberDAOImpl     memberDAO;
    @Mock private MembershipDAOImpl membershipDAO;
    @Mock private PaymentDAOImpl    paymentDAO;
    @Mock private RequestDAOImpl    requestDAO;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberDAO, membershipDAO, requestDAO, paymentDAO);
    }

    // ── registerMember ────────────────────────────────────────────

    @Test
    void registerMember_underAge_throwsBeforeDbCall() {
        Member underage = new MemberBuilder()
                .fullName("Young User")
                .dateOfBirth(LocalDate.now().minusYears(16))
                .gender("FEMALE")
                .phone("5559998877")
                .email("young@test.com")
                .password("pass123")
                .build();

        assertThrows(IllegalArgumentException.class, () ->
                memberService.registerMember(underage, "CLASSIC", "MONTHLY", 1));
        verify(memberDAO, never()).insert(any());
    }

    @Test
    void registerMember_duplicatePhone_throwsIllegalArgument() {
        Member m = buildAdultMember("5551234567", "test@test.com");
        when(memberDAO.existsByPhone("5551234567")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                memberService.registerMember(m, "CLASSIC", "MONTHLY", 1));
        verify(memberDAO, never()).insert(any());
    }

    @Test
    void registerMember_duplicateEmail_throwsIllegalArgument() {
        Member m = buildAdultMember("5551234567", "dup@test.com");
        when(memberDAO.existsByPhone(anyString())).thenReturn(false);
        when(memberDAO.existsByEmail("dup@test.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                memberService.registerMember(m, "CLASSIC", "MONTHLY", 1));
        verify(memberDAO, never()).insert(any());
    }

    @Test
    void registerMember_validMember_insertsAll() {
        Member m = buildAdultMember("5551234567", "new@test.com");
        when(memberDAO.existsByPhone(anyString())).thenReturn(false);
        when(memberDAO.existsByEmail(anyString())).thenReturn(false);

        Member saved = new Member();
        saved.setMemberId(42);
        when(memberDAO.findByPhone("5551234567")).thenReturn(Optional.of(saved));

        memberService.registerMember(m, "CLASSIC", "MONTHLY", 1);

        verify(memberDAO).insert(any(Member.class));
        verify(membershipDAO).insert(any(Membership.class));
        verify(paymentDAO).insert(any());
    }

    // ── createRegistrationRequest ─────────────────────────────────

    @Test
    void createRegistrationRequest_underAge_throwsBeforeDbCall() {
        Member m = new MemberBuilder()
                .fullName("Young User")
                .dateOfBirth(LocalDate.now().minusYears(16))
                .gender("MALE")
                .phone("5559998800")
                .email("young2@test.com")
                .password("pass123")
                .build();

        assertThrows(IllegalArgumentException.class, () ->
                memberService.createRegistrationRequest(m, "CLASSIC", "MONTHLY"));
        verify(memberDAO, never()).insert(any());
    }

    @Test
    void createRegistrationRequest_duplicatePhone_throwsIllegalArgument() {
        Member m = buildAdultMember("5551111111", "unique@test.com");
        when(memberDAO.existsByPhone("5551111111")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                memberService.createRegistrationRequest(m, "GOLD", "MONTHLY"));
        verify(memberDAO, never()).insert(any());
    }

    @Test
    void createRegistrationRequest_validMember_insertsMemberAndRequest() {
        Member m = buildAdultMember("5551111111", "newreq@test.com");
        when(memberDAO.existsByPhone(anyString())).thenReturn(false);
        when(memberDAO.existsByEmail(anyString())).thenReturn(false);

        Member saved = new Member();
        saved.setMemberId(55);
        when(memberDAO.findByPhone("5551111111")).thenReturn(Optional.of(saved));

        memberService.createRegistrationRequest(m, "CLASSIC", "MONTHLY");

        verify(memberDAO).insert(any(Member.class));
        verify(requestDAO).insertRegistration(any());
    }

    // ── approveRegistration ───────────────────────────────────────

    @Test
    void approveRegistration_setsActiveAndCreatesMembership() {
        RegistrationRequest req = new RegistrationRequest();
        req.setRequestId(1);
        req.setMemberId(42);
        req.setTier("GOLD");
        req.setPackageType("MONTHLY");
        req.setAmount(1250.0);

        when(requestDAO.findPendingRegistrations()).thenReturn(List.of(req));

        memberService.approveRegistration(1, 99);

        verify(requestDAO).updateRegistrationStatus(1, "APPROVED");
        verify(memberDAO).updateStatus(42, "ACTIVE");
        verify(membershipDAO).insert(any());
        verify(paymentDAO).insert(any());
    }

    // ── freezeMembership ──────────────────────────────────────────

    @Test
    void freezeMembership_tooFewDays_throwsBeforeDbCall() {
        assertThrows(IllegalArgumentException.class, () ->
                memberService.freezeMembership(1, 3));
        verify(membershipDAO, never()).findById(anyInt());
    }

    @Test
    void freezeMembership_tooManyDays_throwsBeforeDbCall() {
        assertThrows(IllegalArgumentException.class, () ->
                memberService.freezeMembership(1, 45));
        verify(membershipDAO, never()).findById(anyInt());
    }

    @Test
    void freezeMembership_alreadyFrozen_throwsIllegalState() {
        Membership ms = buildMembership("FROZEN", "GOLD", 0, 30);
        when(membershipDAO.findById(1)).thenReturn(Optional.of(ms));

        assertThrows(IllegalStateException.class, () ->
                memberService.freezeMembership(1, 14));
    }

    @Test
    void freezeMembership_classicLimitReached_throwsIllegalState() {
        Membership ms = buildMembership("ACTIVE", "CLASSIC", 1, 30);
        when(membershipDAO.findById(1)).thenReturn(Optional.of(ms));

        assertThrows(IllegalStateException.class, () ->
                memberService.freezeMembership(1, 14));
    }

    @Test
    void freezeMembership_withinThreeDaysOfExpiry_throwsIllegalState() {
        Membership ms = buildMembership("ACTIVE", "GOLD", 0, 2);
        when(membershipDAO.findById(1)).thenReturn(Optional.of(ms));

        assertThrows(IllegalStateException.class, () ->
                memberService.freezeMembership(1, 14));
    }

    // ── upgradeTier ───────────────────────────────────────────────

    @Test
    void upgradeTier_frozenMembership_throwsIllegalState() {
        Membership ms = buildMembership("FROZEN", "CLASSIC", 0, 30);
        when(membershipDAO.findById(1)).thenReturn(Optional.of(ms));

        assertThrows(IllegalStateException.class, () ->
                memberService.upgradeTier(1, "GOLD"));
    }

    @Test
    void upgradeTier_downgrade_throwsIllegalArgument() {
        Membership ms = buildMembership("ACTIVE", "VIP", 0, 30);
        when(membershipDAO.findById(1)).thenReturn(Optional.of(ms));

        assertThrows(IllegalArgumentException.class, () ->
                memberService.upgradeTier(1, "CLASSIC"));
    }

    @Test
    void upgradeTier_sameTier_throwsIllegalArgument() {
        Membership ms = buildMembership("ACTIVE", "GOLD", 0, 30);
        when(membershipDAO.findById(1)).thenReturn(Optional.of(ms));

        assertThrows(IllegalArgumentException.class, () ->
                memberService.upgradeTier(1, "GOLD"));
    }

    @Test
    void upgradeTier_validUpgrade_updatesAndPersists() {
        Membership ms = buildMembership("ACTIVE", "CLASSIC", 0, 30);
        when(membershipDAO.findById(1)).thenReturn(Optional.of(ms));

        memberService.upgradeTier(1, "GOLD");

        verify(membershipDAO).update(ms);
        assertEquals("GOLD", ms.getTier());
    }

    // ── archivePassiveMembers ─────────────────────────────────────

    @Test
    void archivePassiveMembers_noMemberships_archivesImmediately() {
        Member passive = new Member();
        passive.setMemberId(77);
        passive.setStatus("PASSIVE");

        when(memberDAO.findByStatus("PASSIVE")).thenReturn(List.of(passive));
        when(membershipDAO.findAllByMemberId(77)).thenReturn(List.of());

        int count = memberService.archivePassiveMembers();

        assertEquals(1, count);
        verify(memberDAO).updateStatus(77, "ARCHIVED");
        verify(memberDAO).setArchivedAt(77);
    }

    @Test
    void archivePassiveMembers_recentExpiry_doesNotArchive() {
        Member passive = new Member();
        passive.setMemberId(78);

        Membership ms = buildMembership("PASSIVE", "CLASSIC", 0, 0);
        ms.setEndDate(LocalDate.now().minusMonths(3));

        when(memberDAO.findByStatus("PASSIVE")).thenReturn(List.of(passive));
        when(membershipDAO.findAllByMemberId(78)).thenReturn(List.of(ms));

        int count = memberService.archivePassiveMembers();

        assertEquals(0, count);
        verify(memberDAO, never()).updateStatus(anyInt(), anyString());
    }

    @Test
    void archivePassiveMembers_expiredOverSixMonths_archives() {
        Member passive = new Member();
        passive.setMemberId(79);

        Membership ms = buildMembership("PASSIVE", "CLASSIC", 0, 0);
        ms.setEndDate(LocalDate.now().minusMonths(7));

        when(memberDAO.findByStatus("PASSIVE")).thenReturn(List.of(passive));
        when(membershipDAO.findAllByMemberId(79)).thenReturn(List.of(ms));

        int count = memberService.archivePassiveMembers();

        assertEquals(1, count);
        verify(memberDAO).updateStatus(79, "ARCHIVED");
    }

    // ── calculateAmount ───────────────────────────────────────────

    @Test
    void calculateAmount_classic_monthly() {
        assertEquals(750.0, memberService.calculateAmount("CLASSIC", "MONTHLY"), 0.01);
    }

    @Test
    void calculateAmount_gold_monthly() {
        assertEquals(1250.0, memberService.calculateAmount("GOLD", "MONTHLY"), 0.01);
    }

    @Test
    void calculateAmount_vip_annualPrepaid_15pctDiscount() {
        assertEquals(2000.0 * 12 * 0.85, memberService.calculateAmount("VIP", "ANNUAL_PREPAID"), 0.01);
    }

    @Test
    void calculateAmount_classic_annualInstallment_7pctSurcharge() {
        double expected = 750.0 * 12 * 1.07;
        assertEquals(expected, memberService.calculateAmount("CLASSIC", "ANNUAL_INSTALLMENT"), 0.01);
        assertNotEquals(750.0 * 1.07, memberService.calculateAmount("CLASSIC", "ANNUAL_INSTALLMENT"), 0.01);
    }

    // ── Helpers ───────────────────────────────────────────────────

    private Member buildAdultMember(String phone, String email) {
        return new MemberBuilder()
                .fullName("Test User")
                .dateOfBirth(LocalDate.of(1995, 6, 15))
                .gender("MALE")
                .phone(phone)
                .email(email)
                .password("plainpass123")
                .build();
    }

    private Membership buildMembership(String status, String tier, int freezeCount, int daysUntilEnd) {
        Membership ms = new Membership();
        ms.setMembershipId(1);
        ms.setMemberId(10);
        ms.setStatus(status);
        ms.setTier(tier);
        ms.setFreezeCount(freezeCount);
        ms.setEndDate(LocalDate.now().plusDays(daysUntilEnd));
        return ms;
    }
}