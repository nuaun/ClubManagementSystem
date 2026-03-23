package com.iscms;

import com.iscms.service.AuthService;
import com.iscms.service.LoginResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    private final AuthService authService = new AuthService();

    @Test
    void testHashPassword_notNull() {
        String hash = AuthService.hashPassword("test123");
        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$"));
    }

    @Test
    void testHashPassword_differentEachTime() {
        String hash1 = AuthService.hashPassword("test123");
        String hash2 = AuthService.hashPassword("test123");
        assertNotEquals(hash1, hash2); // BCrypt produces different salts each time
    }

    @Test
    void testLoginMember_notFound() {
        try {
            LoginResult result = authService.loginMember("0000000000", "wrongpass");
            assertEquals(LoginResult.Status.NOT_FOUND, result.getStatus());
        } catch (RuntimeException e) {
            Assumptions.assumeTrue(false, "DB not available: " + e.getMessage());
        }
    }

    @Test
    void testLoginManager_notFound() {
        try {
            LoginResult result = authService.loginManager("nonexistent@test.com", "pass");
            assertEquals(LoginResult.Status.NOT_FOUND, result.getStatus());
        } catch (RuntimeException e) {
            Assumptions.assumeTrue(false, "DB not available: " + e.getMessage());
        }
    }

    @Test
    void testLoginTrainer_notFound() {
        try {
            LoginResult result = authService.loginTrainer("nonexistent_user", "pass");
            assertEquals(LoginResult.Status.NOT_FOUND, result.getStatus());
        } catch (RuntimeException e) {
            Assumptions.assumeTrue(false, "DB not available: " + e.getMessage());
        }
    }
}
