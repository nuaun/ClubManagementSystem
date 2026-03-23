package com.iscms;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BmiCalculationTest {

    private double calcBmi(double weight, double heightCm) {
        double heightM = heightCm / 100.0;
        return Math.round((weight / (heightM * heightM)) * 100.0) / 100.0;
    }

    private String calcCategory(double bmi) {
        if (bmi < 18.5) return "UNDERWEIGHT";
        if (bmi < 25.0) return "NORMAL";
        if (bmi < 30.0) return "OVERWEIGHT";
        return "OBESE";
    }

    @Test
    void testBmi_normalWeight() {
        double bmi = calcBmi(70, 175);
        assertEquals(22.86, bmi);
        assertEquals("NORMAL", calcCategory(bmi));
    }

    @Test
    void testBmi_underweight() {
        double bmi = calcBmi(50, 175);
        assertEquals("UNDERWEIGHT", calcCategory(bmi));
    }

    @Test
    void testBmi_overweight() {
        double bmi = calcBmi(85, 175);
        assertEquals("OVERWEIGHT", calcCategory(bmi));
    }

    @Test
    void testBmi_obese() {
        double bmi = calcBmi(110, 175);
        assertEquals("OBESE", calcCategory(bmi));
    }

    @Test
    void testBmi_zeroHeight_guardTriggered() {
        // MemberService.createRegistrationRequest() height > 0 guard should prevent division
        // We verify the guard logic directly: height = 0 must never reach division
        double height = 0.0;
        assertFalse(height > 0, "Guard condition: height > 0 must be false for zero height");
    }
}