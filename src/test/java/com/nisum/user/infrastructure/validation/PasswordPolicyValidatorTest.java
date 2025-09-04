package com.nisum.user.infrastructure.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordPolicyValidatorTest {

    private PasswordPolicyValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PasswordPolicyValidator();
        ReflectionTestUtils.setField(validator, "regex",
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
        validator.initialize(null);
    }

    @Test
    @DisplayName("Return true for valid password matching policy")
    void returnTrueForValidPassword() {
        assertTrue(validator.isValid("Abcdef1@", null));
        assertTrue(validator.isValid("StrongP@ssw0rd", null));
    }

    @Test
    @DisplayName("Return false for password not matching policy")
    void returnFalseForInvalidPassword() {
        assertFalse(validator.isValid("abcdefg1", null));
        assertFalse(validator.isValid("ABCDEFG1", null));
        assertFalse(validator.isValid("Abcdefgh", null));
        assertFalse(validator.isValid("Abcdefg1", null));
        assertFalse(validator.isValid("Abc1@", null));
    }

    @Test
    @DisplayName("Return false for null password")
    void returnFalseForNullPassword() {
        assertFalse(validator.isValid(null, null));
    }
}