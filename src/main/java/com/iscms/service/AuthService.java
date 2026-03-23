package com.iscms.service;

import com.iscms.dao.MemberDAOImpl;
import com.iscms.dao.ManagerDAOImpl;
import com.iscms.dao.TrainerDAOImpl;
import com.iscms.dao.MembershipDAOImpl;
import com.iscms.model.Manager;
import com.iscms.model.Member;
import com.iscms.model.Membership;
import com.iscms.model.Trainer;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDate;
import java.util.Optional;

public class AuthService {

    private final MemberDAOImpl     memberDAO;
    private final ManagerDAOImpl    managerDAO;
    private final TrainerDAOImpl    trainerDAO;
    private final MembershipDAOImpl membershipDAO;

    /** Production constructor. */
    public AuthService() {
        this.memberDAO     = new MemberDAOImpl();
        this.managerDAO    = new ManagerDAOImpl();
        this.trainerDAO    = new TrainerDAOImpl();
        this.membershipDAO = new MembershipDAOImpl();
    }

    /** Package-private constructor for unit tests — allows Mockito to inject mocks. */
    public AuthService(MemberDAOImpl memberDAO, ManagerDAOImpl managerDAO,
                       TrainerDAOImpl trainerDAO, MembershipDAOImpl membershipDAO) {
        this.memberDAO     = memberDAO;
        this.managerDAO    = managerDAO;
        this.trainerDAO    = trainerDAO;
        this.membershipDAO = membershipDAO;
    }

    // 3 wrong → SUGGEST_RESET, 3 more without resetting → LOCKED (total 6)
    private static final int SUGGEST_RESET_AT     = 3;
    private static final int MAX_MEMBER_ATTEMPTS  = 6;
    private static final int MAX_MANAGER_ATTEMPTS = 5;
    private static final int MAX_TRAINER_ATTEMPTS = 5;

    // ── MEMBER LOGIN ──────────────────────────────────────────────
    public LoginResult loginMember(String phone, String password) {
        Optional<Member> opt = memberDAO.findByPhone(phone);
        if (opt.isEmpty()) return LoginResult.NOT_FOUND;

        // Use a mutable holder so we can re-fetch after unfreeze without breaking lambda effectively-final rules
        Member member = opt.get();

        if (member.getStatus().equals("SUSPENDED"))            return LoginResult.SUSPENDED;
        if (member.getStatus().equals("ARCHIVED"))             return LoginResult.ARCHIVED;
        if (member.getStatus().equals("PENDING"))              return LoginResult.PENDING;
        if (member.getStatus().equals("REGISTRATION_FAILED"))  return LoginResult.REGISTRATION_FAILED;

        // BUG-3 FIX: if freeze period has elapsed, unfreeze at login time
        if (member.getStatus().equals("FROZEN")) {
            Optional<Membership> frozenOpt = membershipDAO.findFrozenByMemberId(member.getMemberId());
            if (frozenOpt.isPresent() && !LocalDate.now().isBefore(frozenOpt.get().getEndDate())) {
                membershipDAO.updateStatus(frozenOpt.get().getMembershipId(), "ACTIVE");
                memberDAO.updateStatus(member.getMemberId(), "ACTIVE");
                // Re-fetch — assign to new final variable, original 'member' is no longer used below
                member = memberDAO.findByPhone(phone).orElse(member);
            } else {
                return LoginResult.FROZEN;
            }
        }

        // After potential unfreeze, capture into effectively-final variable for lambda use
        final Member currentMember = member;

        if (currentMember.getStatus().equals("PASSIVE")) return LoginResult.PASSIVE;
        if (currentMember.isLocked())                    return LoginResult.LOCKED;

        if (!BCrypt.checkpw(password, currentMember.getPassword())) {
            int attempts = currentMember.getFailedAttempts() + 1;
            memberDAO.updateFailedAttempts(currentMember.getMemberId(), attempts);

            if (attempts >= MAX_MEMBER_ATTEMPTS) {
                memberDAO.updateLockStatus(currentMember.getMemberId(), true);
                return LoginResult.LOCKED;
            }
            if (attempts == SUGGEST_RESET_AT) return LoginResult.SUGGEST_RESET;

            int remaining = attempts < SUGGEST_RESET_AT
                    ? (SUGGEST_RESET_AT - attempts)
                    : (MAX_MEMBER_ATTEMPTS - attempts);
            return LoginResult.wrong(remaining);
        }

        // Correct password — reset counter
        memberDAO.updateFailedAttempts(currentMember.getMemberId(), 0);

        // FIX-BUG4: Grace period check — if ANNUAL_INSTALLMENT expired, set PASSIVE before returning
        final boolean[] madePassive = {false};
        membershipDAO.findActiveByMemberId(currentMember.getMemberId()).ifPresent(ms -> {
            if ("ANNUAL_INSTALLMENT".equals(ms.getPackageType())
                    && ms.getEndDate().plusDays(3).isBefore(LocalDate.now())) {
                membershipDAO.updateStatus(ms.getMembershipId(), "PASSIVE");
                memberDAO.updateStatus(currentMember.getMemberId(), "PASSIVE");
                madePassive[0] = true;
            }
        });
        if (madePassive[0]) return LoginResult.PASSIVE;

        // Re-fetch member after potential status change
        Member refreshed = memberDAO.findByPhone(phone).orElse(currentMember);
        return LoginResult.success(refreshed);
    }

    // ── MANAGER LOGIN ─────────────────────────────────────────────
    public LoginResult loginManager(String email, String password) {
        Optional<Manager> opt = managerDAO.findByEmail(email);
        if (opt.isEmpty()) return LoginResult.NOT_FOUND;

        Manager manager = opt.get();
        if (manager.isLocked()) return LoginResult.LOCKED;

        if (!BCrypt.checkpw(password, manager.getPassword())) {
            int attempts = manager.getFailedAttempts() + 1;
            managerDAO.updateFailedAttempts(manager.getManagerId(), attempts);
            if (attempts >= MAX_MANAGER_ATTEMPTS) {
                managerDAO.updateLockStatus(manager.getManagerId(), true);
                return LoginResult.LOCKED;
            }
            return LoginResult.wrong(MAX_MANAGER_ATTEMPTS - attempts);
        }

        managerDAO.updateFailedAttempts(manager.getManagerId(), 0);
        return LoginResult.success(manager);
    }

    // ── TRAINER LOGIN ─────────────────────────────────────────────
    public LoginResult loginTrainer(String username, String password) {
        Optional<Trainer> opt = trainerDAO.findByUsername(username);
        if (opt.isEmpty()) return LoginResult.NOT_FOUND;

        Trainer trainer = opt.get();
        if (trainer.isLocked())   return LoginResult.LOCKED;
        if (!trainer.isActive())  return LoginResult.SUSPENDED;

        if (!BCrypt.checkpw(password, trainer.getPassword())) {
            int attempts = trainer.getFailedAttempts() + 1;
            trainerDAO.updateFailedAttempts(trainer.getTrainerId(), attempts);
            if (attempts >= MAX_TRAINER_ATTEMPTS) {
                trainerDAO.updateLockStatus(trainer.getTrainerId(), true);
                return LoginResult.LOCKED;
            }
            return LoginResult.wrong(MAX_TRAINER_ATTEMPTS - attempts);
        }

        trainerDAO.updateFailedAttempts(trainer.getTrainerId(), 0);
        return LoginResult.success(trainer);
    }

    // ── PASSWORD RESET ────────────────────────────────────────────
    public boolean resetMemberPassword(String phone, String newPassword) {
        Optional<Member> opt = memberDAO.findByPhone(phone);
        if (opt.isEmpty()) return false;
        String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        memberDAO.resetPassword(opt.get().getMemberId(), hashed);
        return true;
    }

    public boolean resetManagerPassword(int managerId, String newPassword) {
        String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        managerDAO.updatePassword(managerId, hashed);
        return true;
    }

    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }
}