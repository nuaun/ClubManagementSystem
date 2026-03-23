package com.iscms;

import com.iscms.model.Member;
import com.iscms.model.MemberBuilder;
import com.iscms.service.MemberService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;

public class MemberServiceTest {

    private final MemberService memberService = new MemberService();

    // ── DB'siz çalışan testler (iş kuralı validasyonu DB'den önce gelir) ──

    @Test
    void testRegisterMember_underAge_throwsException() {
        Member m = new MemberBuilder()
                .fullName("Test User")
                .dateOfBirth(LocalDate.now().minusYears(16))
                .gender("MALE")
                .phone("5559998877")
                .email("test@test.com")
                .password("test12345")
                .build();
        // Age check fires before existsByPhone DB call — but if DB is unavailable
        // the test may throw RuntimeException from DAO. We guard with Assumptions.
        try {
            assertThrows(IllegalArgumentException.class, () ->
                    memberService.registerMember(m, "CLASSIC", "MONTHLY", 1));
        } catch (RuntimeException e) {
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "DB not available: " + e.getMessage());
        }
    }

    @Test
    // FIX-KRİTİK-2: days validasyonu artık findById'dan ÖNCE yapılıyor.
    // Bu test DB olmadan da geçer — IllegalArgumentException doğrudan fırlatılır.
    void testFreezeMembership_tooFewDays_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                memberService.freezeMembership(999999, 5)); // < 7 gün → DB'ye gitmeden hata
    }

    @Test
    void testFreezeMembership_tooManyDays_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                memberService.freezeMembership(999999, 31)); // > 30 gün → DB'ye gitmeden hata
    }

    // ── calculateAmount — DB gerektirmez, saf hesap ──

    @Test
    void testCalculateAmount_classic_monthly() {
        double amount = memberService.calculateAmount("CLASSIC", "MONTHLY");
        assertEquals(750.0, amount, 0.01);
    }

    @Test
    void testCalculateAmount_gold_monthly() {
        double amount = memberService.calculateAmount("GOLD", "MONTHLY");
        assertEquals(1250.0, amount, 0.01);
    }

    @Test
    void testCalculateAmount_vip_annualPrepaid() {
        double amount = memberService.calculateAmount("VIP", "ANNUAL_PREPAID");
        assertEquals(2000.0 * 12 * 0.85, amount, 0.01);
    }

    @Test
    // FIX: ANNUAL_INSTALLMENT artık 12 ay × %7 — eski monthly*1.07 hatalıydı
    void testCalculateAmount_classic_annualInstallment() {
        double amount = memberService.calculateAmount("CLASSIC", "ANNUAL_INSTALLMENT");
        assertEquals(750.0 * 12 * 1.07, amount, 0.01); // 9630.0
        assertNotEquals(750.0 * 1.07, amount, 0.01);   // eski hatalı değer
    }
}
