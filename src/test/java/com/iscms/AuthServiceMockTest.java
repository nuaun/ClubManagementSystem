package com.iscms;

import com.iscms.dao.MemberDAOImpl;
import com.iscms.dao.ManagerDAOImpl;
import com.iscms.dao.MembershipDAOImpl;
import com.iscms.dao.TrainerDAOImpl;
import com.iscms.model.Manager;
import com.iscms.model.Member;
import com.iscms.model.Trainer;
import com.iscms.service.AuthService;
import com.iscms.service.LoginResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceMockTest {

    @Mock private MemberDAOImpl     memberDAO;
    @Mock private ManagerDAOImpl    managerDAO;
    @Mock private TrainerDAOImpl    trainerDAO;
    @Mock private MembershipDAOImpl membershipDAO;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(memberDAO, managerDAO, trainerDAO, membershipDAO);
    }

    // ── MEMBER LOGIN ──────────────────────────────────────────────

    @Test
    void loginMember_unknownPhone_returnsNotFound() {
        when(memberDAO.findByPhone("0000000000")).thenReturn(Optional.empty());
        assertEquals(LoginResult.Status.NOT_FOUND,
                authService.loginMember("0000000000", "any").getStatus());
    }

    @Test
    void loginMember_suspendedMember_returnsSuspended() {
        Member m = member("SUSPENDED", false, 0, "hash");
        when(memberDAO.findByPhone("5551111111")).thenReturn(Optional.of(m));
        assertEquals(LoginResult.Status.SUSPENDED,
                authService.loginMember("5551111111", "any").getStatus());
    }

    @Test
    void loginMember_pendingMember_returnsPending() {
        Member m = member("PENDING", false, 0, "hash");
        when(memberDAO.findByPhone("5552222222")).thenReturn(Optional.of(m));
        assertEquals(LoginResult.Status.PENDING,
                authService.loginMember("5552222222", "any").getStatus());
    }

    @Test
    void loginMember_archivedMember_returnsArchived() {
        Member m = member("ARCHIVED", false, 0, "hash");
        when(memberDAO.findByPhone("5553333333")).thenReturn(Optional.of(m));
        assertEquals(LoginResult.Status.ARCHIVED,
                authService.loginMember("5553333333", "any").getStatus());
    }

    @Test
    void loginMember_lockedMember_returnsLocked() {
        Member m = member("ACTIVE", true, 6, "hash");
        when(memberDAO.findByPhone("5554444444")).thenReturn(Optional.of(m));
        assertEquals(LoginResult.Status.LOCKED,
                authService.loginMember("5554444444", "any").getStatus());
    }

    @Test
    void loginMember_wrongPassword_incrementsAttempts() {
        String hashed = AuthService.hashPassword("correctPass");
        Member m = member("ACTIVE", false, 0, hashed);
        when(memberDAO.findByPhone("5555555555")).thenReturn(Optional.of(m));

        LoginResult result = authService.loginMember("5555555555", "wrongPass");

        assertEquals(LoginResult.Status.WRONG_PASSWORD, result.getStatus());
        verify(memberDAO).updateFailedAttempts(m.getMemberId(), 1);
    }

    @Test
    void loginMember_thirdWrongAttempt_returnsSuggestReset() {
        String hashed = AuthService.hashPassword("correctPass");
        Member m = member("ACTIVE", false, 2, hashed);
        when(memberDAO.findByPhone("5556666666")).thenReturn(Optional.of(m));

        assertEquals(LoginResult.Status.SUGGEST_RESET,
                authService.loginMember("5556666666", "wrongPass").getStatus());
    }

    @Test
    void loginMember_sixthWrongAttempt_locksAccount() {
        String hashed = AuthService.hashPassword("correctPass");
        Member m = member("ACTIVE", false, 5, hashed);
        when(memberDAO.findByPhone("5557777777")).thenReturn(Optional.of(m));

        LoginResult result = authService.loginMember("5557777777", "wrongPass");

        assertEquals(LoginResult.Status.LOCKED, result.getStatus());
        verify(memberDAO).updateLockStatus(m.getMemberId(), true);
    }

    @Test
    void loginMember_correctPassword_returnsSuccess() {
        String hashed = AuthService.hashPassword("myPassword");
        Member m = member("ACTIVE", false, 0, hashed);
        when(memberDAO.findByPhone("5558888888")).thenReturn(Optional.of(m));
        when(membershipDAO.findActiveByMemberId(m.getMemberId())).thenReturn(Optional.empty());
        when(memberDAO.findByPhone("5558888888")).thenReturn(Optional.of(m));

        LoginResult result = authService.loginMember("5558888888", "myPassword");

        assertEquals(LoginResult.Status.SUCCESS, result.getStatus());
        verify(memberDAO).updateFailedAttempts(m.getMemberId(), 0);
    }

    // ── MANAGER LOGIN ─────────────────────────────────────────────

    @Test
    void loginManager_unknownEmail_returnsNotFound() {
        when(managerDAO.findByEmail("nobody@test.com")).thenReturn(Optional.empty());
        assertEquals(LoginResult.Status.NOT_FOUND,
                authService.loginManager("nobody@test.com", "any").getStatus());
    }

    @Test
    void loginManager_lockedAccount_returnsLocked() {
        Manager m = manager(true, 5);
        when(managerDAO.findByEmail("m@test.com")).thenReturn(Optional.of(m));
        assertEquals(LoginResult.Status.LOCKED,
                authService.loginManager("m@test.com", "any").getStatus());
    }

    @Test
    void loginManager_wrongPassword_incrementsAttempts() {
        String hashed = AuthService.hashPassword("correct");
        Manager m = manager(false, 0);
        m.setPassword(hashed);
        when(managerDAO.findByEmail("m@test.com")).thenReturn(Optional.of(m));

        LoginResult result = authService.loginManager("m@test.com", "wrong");

        assertEquals(LoginResult.Status.WRONG_PASSWORD, result.getStatus());
        verify(managerDAO).updateFailedAttempts(m.getManagerId(), 1);
    }

    @Test
    void loginManager_fifthWrongAttempt_locksAccount() {
        String hashed = AuthService.hashPassword("correct");
        Manager m = manager(false, 4);
        m.setPassword(hashed);
        when(managerDAO.findByEmail("m@test.com")).thenReturn(Optional.of(m));

        LoginResult result = authService.loginManager("m@test.com", "wrong");

        assertEquals(LoginResult.Status.LOCKED, result.getStatus());
        verify(managerDAO).updateLockStatus(m.getManagerId(), true);
    }

    @Test
    void loginManager_correctPassword_returnsSuccess() {
        String hashed = AuthService.hashPassword("correct");
        Manager m = manager(false, 0);
        m.setPassword(hashed);
        when(managerDAO.findByEmail("m@test.com")).thenReturn(Optional.of(m));

        LoginResult result = authService.loginManager("m@test.com", "correct");

        assertEquals(LoginResult.Status.SUCCESS, result.getStatus());
        verify(managerDAO).updateFailedAttempts(m.getManagerId(), 0);
    }

    // ── TRAINER LOGIN ─────────────────────────────────────────────

    @Test
    void loginTrainer_unknownUsername_returnsNotFound() {
        when(trainerDAO.findByUsername("nobody")).thenReturn(Optional.empty());
        assertEquals(LoginResult.Status.NOT_FOUND,
                authService.loginTrainer("nobody", "any").getStatus());
    }

    @Test
    void loginTrainer_lockedAccount_returnsLocked() {
        Trainer t = trainer(true, true, 5);
        when(trainerDAO.findByUsername("trainer1")).thenReturn(Optional.of(t));
        assertEquals(LoginResult.Status.LOCKED,
                authService.loginTrainer("trainer1", "any").getStatus());
    }

    @Test
    void loginTrainer_inactiveTrainer_returnsSuspended() {
        Trainer t = trainer(false, false, 0);
        when(trainerDAO.findByUsername("trainer1")).thenReturn(Optional.of(t));
        assertEquals(LoginResult.Status.SUSPENDED,
                authService.loginTrainer("trainer1", "any").getStatus());
    }

    @Test
    void loginTrainer_wrongPassword_incrementsAttempts() {
        String hashed = AuthService.hashPassword("correct");
        Trainer t = trainer(false, true, 0);
        t.setPassword(hashed);
        when(trainerDAO.findByUsername("trainer1")).thenReturn(Optional.of(t));

        LoginResult result = authService.loginTrainer("trainer1", "wrong");

        assertEquals(LoginResult.Status.WRONG_PASSWORD, result.getStatus());
        verify(trainerDAO).updateFailedAttempts(t.getTrainerId(), 1);
    }

    @Test
    void loginTrainer_correctPassword_returnsSuccess() {
        String hashed = AuthService.hashPassword("correct");
        Trainer t = trainer(false, true, 0);
        t.setPassword(hashed);
        when(trainerDAO.findByUsername("trainer1")).thenReturn(Optional.of(t));

        LoginResult result = authService.loginTrainer("trainer1", "correct");

        assertEquals(LoginResult.Status.SUCCESS, result.getStatus());
        verify(trainerDAO).updateFailedAttempts(t.getTrainerId(), 0);
    }

    // ── hashPassword ──────────────────────────────────────────────

    @Test
    void hashPassword_producesValidBcryptPrefix() {
        String hash = AuthService.hashPassword("test123");
        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$"));
    }

    @Test
    void hashPassword_differentSaltEachTime() {
        assertNotEquals(
                AuthService.hashPassword("same"),
                AuthService.hashPassword("same"));
    }

    // ── Helpers ───────────────────────────────────────────────────

    private Member member(String status, boolean locked, int failedAttempts, String password) {
        Member m = new Member();
        m.setMemberId(99);
        m.setStatus(status);
        m.setLocked(locked);
        m.setFailedAttempts(failedAttempts);
        m.setPassword(password);
        m.setDateOfBirth(LocalDate.of(1990, 1, 1));
        return m;
    }

    private Manager manager(boolean locked, int failedAttempts) {
        Manager m = new Manager();
        m.setManagerId(10);
        m.setLocked(locked);
        m.setFailedAttempts(failedAttempts);
        m.setPassword("placeholder");
        return m;
    }

    private Trainer trainer(boolean locked, boolean active, int failedAttempts) {
        Trainer t = new Trainer();
        t.setTrainerId(20);
        t.setLocked(locked);
        t.setActive(active);
        t.setFailedAttempts(failedAttempts);
        t.setPassword("placeholder");
        return t;
    }
}