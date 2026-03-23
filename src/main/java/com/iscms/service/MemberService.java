package com.iscms.service;

import com.iscms.dao.*;
import com.iscms.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class MemberService {

    private final MemberDAOImpl     memberDAO;
    private final MembershipDAOImpl membershipDAO;
    private final RequestDAOImpl    requestDAO;
    private final PaymentDAOImpl    paymentDAO;

    /** Production constructor — creates real DAO instances. */
    public MemberService() {
        this.memberDAO     = new MemberDAOImpl();
        this.membershipDAO = new MembershipDAOImpl();
        this.requestDAO    = new RequestDAOImpl();
        this.paymentDAO    = new PaymentDAOImpl();
    }

    /** Package-private constructor for unit tests — allows Mockito to inject mocks. */
    public MemberService(MemberDAOImpl memberDAO, MembershipDAOImpl membershipDAO,
                         RequestDAOImpl requestDAO, PaymentDAOImpl paymentDAO) {
        this.memberDAO     = memberDAO;
        this.membershipDAO = membershipDAO;
        this.requestDAO    = requestDAO;
        this.paymentDAO    = paymentDAO;
    }

    // ── REGISTRATION REQUEST (3 day approval) ────────────────────
    public void createRegistrationRequest(Member member, String tier, String packageType) {

        if (member.getDateOfBirth() == null) {
            throw new IllegalArgumentException("Date of birth is required.");
        }
        if (member.getPhone() == null || member.getPhone().isBlank()) {
            throw new IllegalArgumentException("Phone number is required.");
        }

        if (member.getDateOfBirth().isAfter(LocalDate.now().minusYears(18))) {
            throw new IllegalArgumentException("Minimum age is 18.");
        }
        if (memberDAO.existsByPhone(member.getPhone())) {
            throw new IllegalArgumentException("Phone number already registered.");
        }
        if (memberDAO.existsByEmail(member.getEmail())) {
            throw new IllegalArgumentException("Email address already registered.");
        }
        if (member.getWeight() != null && member.getHeight() != null && member.getHeight() > 0) {
            double heightM = member.getHeight() / 100.0;
            double bmi = Math.round((member.getWeight() / (heightM * heightM)) * 100.0) / 100.0;
            member.setBmiValue(bmi);
            member.setBmiCategory(calcBmiCategory(bmi));
        }
        // Double-hashing guard: if password already starts with BCrypt prefix, skip
        if (!member.getPassword().startsWith("$2a$")) {
            member.setPassword(AuthService.hashPassword(member.getPassword()));
        }
        member.setStatus("PENDING");
        memberDAO.insert(member);

        Member saved = memberDAO.findByPhone(member.getPhone()).orElseThrow();

        RegistrationRequest req = new RegistrationRequest();
        req.setMemberId(saved.getMemberId());
        req.setTier(tier);
        req.setPackageType(packageType);
        req.setAmount(calcAmount(tier, packageType));
        req.setExpiresAt(LocalDateTime.now().plusDays(3));
        requestDAO.insertRegistration(req);
    }

    // ── REGISTER MEMBER DIRECT (Manager adds directly) ───────────
    public void registerMember(Member member, String tier, String packageType, int managerId) {
        if (member.getDateOfBirth() == null) {
            throw new IllegalArgumentException("Date of birth is required.");
        }
        if (member.getPhone() == null || member.getPhone().isBlank()) {
            throw new IllegalArgumentException("Phone number is required.");
        }

        if (member.getDateOfBirth().isAfter(LocalDate.now().minusYears(18))) {
            throw new IllegalArgumentException("Minimum age is 18.");
        }
        if (memberDAO.existsByPhone(member.getPhone())) {
            throw new IllegalArgumentException("Phone number already registered.");
        }
        if (memberDAO.existsByEmail(member.getEmail())) {
            throw new IllegalArgumentException("Email address already registered.");
        }
        if (member.getWeight() != null && member.getHeight() != null && member.getHeight() > 0) {
            double heightM = member.getHeight() / 100.0;
            double bmi = Math.round((member.getWeight() / (heightM * heightM)) * 100.0) / 100.0;
            member.setBmiValue(bmi);
            member.setBmiCategory(calcBmiCategory(bmi));
        }
        // FIX-BUG8: Çift hashing koruması
        if (!member.getPassword().startsWith("$2a$")) {
            member.setPassword(AuthService.hashPassword(member.getPassword()));
        }
        // Not: production'da bu blok tek bir DB transaction içinde olmalıdır.
// JDBC transaction: conn.setAutoCommit(false) → işlemler → conn.commit() / conn.rollback()
        member.setStatus("ACTIVE");
        memberDAO.insert(member);

        Member saved = memberDAO.findByPhone(member.getPhone()).orElseThrow();

        Membership ms = new Membership();
        ms.setMemberId(saved.getMemberId());
        ms.setTier(tier);
        ms.setPackageType(packageType);
        ms.setStartDate(LocalDate.now());
        ms.setEndDate(calcEndDate(packageType));
        ms.setStatus("ACTIVE");
        ms.setFreezeCount(0);
        membershipDAO.insert(ms);

        Payment payment = new Payment();
        payment.setMemberId(saved.getMemberId());
        payment.setAmount(calcAmount(tier, packageType));
        payment.setPaymentType("MEMBERSHIP");
        payment.setDescription(tier + " - " + packageType);
        payment.setStatus("PAID");
        payment.setRecordedBy(managerId);
        paymentDAO.insert(payment); // FIX-4
    }

    // ── APPROVE REGISTRATION ──────────────────────────────────────
    public void approveRegistration(int requestId, int recordedBy) {
        List<RegistrationRequest> reqs = requestDAO.findPendingRegistrations();
        RegistrationRequest req = reqs.stream()
                .filter(r -> r.getRequestId() == requestId)
                .findFirst().orElseThrow();

        requestDAO.updateRegistrationStatus(requestId, "APPROVED");
        memberDAO.updateStatus(req.getMemberId(), "ACTIVE");

        Membership ms = new Membership();
        ms.setMemberId(req.getMemberId());
        ms.setTier(req.getTier());
        ms.setPackageType(req.getPackageType());
        ms.setStartDate(LocalDate.now());
        ms.setEndDate(calcEndDate(req.getPackageType()));
        ms.setStatus("ACTIVE");
        ms.setFreezeCount(0);
        membershipDAO.insert(ms);

        Payment payment = new Payment();
        payment.setMemberId(req.getMemberId());
        payment.setAmount(req.getAmount());
        payment.setPaymentType("MEMBERSHIP");
        payment.setDescription(req.getTier() + " - " + req.getPackageType());
        payment.setStatus("PAID");
        payment.setRecordedBy(recordedBy);
        paymentDAO.insert(payment); // FIX-4
    }

    // ── FAIL REGISTRATION ─────────────────────────────────────────
    public void failRegistration(int requestId) {
        List<RegistrationRequest> reqs = requestDAO.findPendingRegistrations();
        reqs.stream()
                .filter(r -> r.getRequestId() == requestId)
                .findFirst()
                .ifPresent(req -> {
                    requestDAO.updateRegistrationStatus(requestId, "FAILED");
                    memberDAO.updateStatus(req.getMemberId(), "REGISTRATION_FAILED");
                });
    }

    // ── TIER UPGRADE REQUEST ──────────────────────────────────────
    public void createTierUpgradeRequest(int memberId, int membershipId,
                                         String currentTier, String newTier, double fee) {
        TierUpgradeRequest req = new TierUpgradeRequest();
        req.setMemberId(memberId);
        req.setMembershipId(membershipId);
        req.setCurrentTier(currentTier);
        req.setRequestedTier(newTier);
        req.setUpgradeFee(fee);
        req.setExpiresAt(LocalDateTime.now().plusDays(3));
        requestDAO.insertTierUpgrade(req);
    }

    // ── APPROVE TIER UPGRADE ──────────────────────────────────────
    public void approveTierUpgrade(int requestId, int recordedBy) {
        List<TierUpgradeRequest> reqs = requestDAO.findPendingTierUpgrades();
        TierUpgradeRequest req = reqs.stream()
                .filter(r -> r.getRequestId() == requestId)
                .findFirst().orElseThrow();

        requestDAO.updateTierUpgradeStatus(requestId, "APPROVED");
        upgradeTier(req.getMembershipId(), req.getRequestedTier());

        Payment payment = new Payment();
        payment.setMemberId(req.getMemberId());
        payment.setAmount(req.getUpgradeFee());
        payment.setPaymentType("UPGRADE");
        payment.setDescription(req.getCurrentTier() + " → " + req.getRequestedTier());
        payment.setStatus("PAID");
        payment.setRecordedBy(recordedBy);
        paymentDAO.insert(payment); // FIX-4
    }

    // ── FAIL TIER UPGRADE ─────────────────────────────────────────
    public void failTierUpgrade(int requestId) {
        requestDAO.updateTierUpgradeStatus(requestId, "FAILED");
    }

    // ── EXPIRE OLD REQUESTS ───────────────────────────────────────
    // BUG-2 FIX: expireOldRegistrations() only updated registration_request.status.
    // member.status was left as 'PENDING' forever — login would show PENDING message
    // even though the request had expired. Now we sync member.status BEFORE marking FAILED.
    public void expireOldRequests() {
        requestDAO.expireOldTierUpgrades();
        // Sync member.status first, then mark requests as FAILED
        List<RegistrationRequest> expired = requestDAO.findExpiredPendingRegistrations();
        for (RegistrationRequest req : expired) {
            memberDAO.updateStatus(req.getMemberId(), "REGISTRATION_FAILED");
        }
        requestDAO.expireOldRegistrations();
    }

    public List<RegistrationRequest> getPendingRegistrations() {
        return requestDAO.findPendingRegistrations();
    }

    public List<TierUpgradeRequest> getPendingTierUpgrades() {
        return requestDAO.findPendingTierUpgrades();
    }

    // ── FREEZE ────────────────────────────────────────────────────
    public void freezeMembership(int membershipId, int days) {
        // FIX-KRİTİK-1a: days validasyonu findById'dan ÖNCE — DB'siz test edilebilsin
        if (days < 7 || days > 30)
            throw new IllegalArgumentException("Freeze duration must be between 7 and 30 days.");

        Membership ms = membershipDAO.findById(membershipId).orElseThrow();

        // FIX-FIX-11 (önceki): ACTIVE kontrolü
        if (!"ACTIVE".equals(ms.getStatus()))
            throw new IllegalStateException("Only ACTIVE memberships can be frozen. Current status: " + ms.getStatus());

        int maxFreeze = switch (ms.getTier()) {
            case "CLASSIC" -> 1;
            case "GOLD"    -> 2;
            case "VIP"     -> 3;
            default        -> 1;
        };
        if (ms.getFreezeCount() >= maxFreeze)
            throw new IllegalStateException("Freeze limit reached for this period.");
        if (ms.getEndDate().minusDays(3).isBefore(LocalDate.now()))
            throw new IllegalStateException("Cannot freeze within 3 days of expiry (BR-08).");

        ms.setEndDate(ms.getEndDate().plusDays(days));
        ms.setStatus("FROZEN");
        membershipDAO.update(ms);
        membershipDAO.incrementFreezeCount(membershipId);

        // FIX-KRİTİK-1b: member tablosu da güncellenmeli — login akışı buradan okur
        memberDAO.updateStatus(ms.getMemberId(), "FROZEN");
    }

    // ── UPGRADE TIER ──────────────────────────────────────────────
    public void upgradeTier(int membershipId, String newTier) {
        Membership ms = membershipDAO.findById(membershipId).orElseThrow();
        // FIX-UYARI-3: FROZEN/PASSIVE üyelik upgrade edilemesin
        if (!"ACTIVE".equals(ms.getStatus()))
            throw new IllegalStateException("Only ACTIVE memberships can be upgraded (BR-09).");
        if (tierRank(newTier) <= tierRank(ms.getTier()))
            throw new IllegalArgumentException("Tier can only be upgraded (BR-09).");
        ms.setTier(newTier);
        membershipDAO.update(ms);
    }

    // ── BMI RECALCULATE ───────────────────────────────────────────
    public void recalculateBmi(int memberId) {
        memberDAO.findById(memberId).ifPresent(m -> {
            if (m.getWeight() != null && m.getHeight() != null && m.getHeight() > 0) {
                double heightM = m.getHeight() / 100.0;
                double bmi = Math.round((m.getWeight() / (heightM * heightM)) * 100.0) / 100.0;
                m.setBmiValue(bmi);
                m.setBmiCategory(calcBmiCategory(bmi));
                memberDAO.update(m);
            }
        });
    }

    public List<Member> getAllMembers()               { return memberDAO.findAll(); }
    public List<Member> getMembersByStatus(String s)  { return memberDAO.findByStatus(s); }
    public Optional<Member> getMemberById(int id)     { return memberDAO.findById(id); }
    public void updateMemberStatus(int id, String s)  { memberDAO.updateStatus(id, s); }
    public void updateMember(Member member)           { memberDAO.update(member); }
    public void unlockMember(int memberId) {
        memberDAO.updateLockStatus(memberId, false);
        memberDAO.updateFailedAttempts(memberId, 0);
    }

    public Optional<Membership> getActiveMembership(int memberId) {
        return membershipDAO.findActiveByMemberId(memberId);
    }

    public List<Membership> getAllMemberships(int memberId) {
        return membershipDAO.findAllByMemberId(memberId);
    }

    public void submitRegistrationRequest(RegistrationRequest req) {
        requestDAO.insertRegistration(req);
    }

    // ── HELPERS ───────────────────────────────────────────────────
    private LocalDate calcEndDate(String packageType) {
        return switch (packageType) {
            case "MONTHLY"             -> LocalDate.now().plusDays(30);
            case "ANNUAL_INSTALLMENT",
                 "ANNUAL_PREPAID"         -> LocalDate.now().plusDays(365);
            default -> LocalDate.now().plusDays(30);
        };
    }

    // FIX-6: public yapıldı — UI (RegisterFrame) buraya delege eder, kopyalamaz.
    // FIX (calcAmount): ANNUAL_INSTALLMENT'da monthly*1.07 yanlıştı (1 ay gibi hesaplıyordu).
    //   Doğrusu: aylık ücret × 12 ay × %7 vade farkı = monthly * 12 * 1.07
    public double calculateAmount(String tier, String packageType) {
        double monthly = switch (tier) {
            case "GOLD" -> 1250.0;
            case "VIP"  -> 2000.0;
            default     -> 750.0;
        };
        return switch (packageType) {
            case "ANNUAL_PREPAID"     -> monthly * 12 * 0.85;
            case "ANNUAL_INSTALLMENT" -> monthly * 12 * 1.07; // FIX: 12 ay × %7 vade
            default                   -> monthly;
        };
    }

    // İç kullanım için alias — internal çağrılar değişmesin
    private double calcAmount(String tier, String packageType) {
        return calculateAmount(tier, packageType);
    }

    private int tierRank(String tier) {
        return switch (tier) {
            case "CLASSIC" -> 1;
            case "GOLD"    -> 2;
            case "VIP"     -> 3;
            default        -> 0;
        };
    }

    private String calcBmiCategory(double bmi) {
        if (bmi < 18.5) return "UNDERWEIGHT";
        if (bmi < 25.0) return "NORMAL";
        if (bmi < 30.0) return "OVERWEIGHT";
        return "OBESE";
    }

    // BR-17: Check and apply grace period for installment payments
    public void checkInstallmentGracePeriod(int memberId) {
        membershipDAO.findActiveByMemberId(memberId).ifPresent(ms -> {
            if (!"ANNUAL_INSTALLMENT".equals(ms.getPackageType())) return;
            // If end date passed grace period (3 days after last expected payment)
            if (ms.getEndDate().plusDays(3).isBefore(LocalDate.now())) {
                membershipDAO.updateStatus(ms.getMembershipId(), "PASSIVE");
                memberDAO.updateStatus(memberId, "PASSIVE");
            }
        });
    }

    // BR-28: Auto-archive passive members (6 months after expiry)
    public int archivePassiveMembers() {
        List<Member> passiveMembers = memberDAO.findByStatus("PASSIVE");
        int count = 0;
        for (Member m : passiveMembers) {
            List<Membership> memberships = membershipDAO.findAllByMemberId(m.getMemberId());
            // FIX-UYARI-2: Üyeliği hiç olmayan passive member sonsuza dek arşivlenemez kalıyordu.
            // Bu üyeler KVKK kapsamına da giremezdi — şimdi direkt arşivleniyor.
            if (memberships.isEmpty()) {
                memberDAO.updateStatus(m.getMemberId(), "ARCHIVED");
                memberDAO.setArchivedAt(m.getMemberId());
                count++;
                continue;
            }
            // FIX-BUG6: DAO'nun "ORDER BY created_at DESC" sıralamasına güvenmek yerine
            // sıralamayı kodda garantiliyoruz — DAO değişirse arşivleme bozulmasın.
            memberships.sort(java.util.Comparator.comparing(Membership::getEndDate).reversed());
            Membership last = memberships.get(0); // en son biten üyelik
            if (last.getEndDate().plusMonths(6).isBefore(LocalDate.now())) {
                memberDAO.updateStatus(m.getMemberId(), "ARCHIVED");
                memberDAO.setArchivedAt(m.getMemberId());
                count++;
            }
        }
        return count;
    }

    // BR-29: KVKK - anonymize members archived 2+ years ago
    public int anonymizeArchivedMembers() {
        LocalDateTime cutoff = LocalDateTime.now().minusYears(2);
        List<Integer> ids = memberDAO.findArchivedMemberIdsBefore(cutoff);
        for (int id : ids) {
            memberDAO.anonymize(id);
        }
        return ids.size();
    }
}