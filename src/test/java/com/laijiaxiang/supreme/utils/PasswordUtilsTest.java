package com.laijiaxiang.supreme.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilsTest {


    @Test
    void encodePassword_WithValidInput_ShouldReturnHashedPassword() {
        // Given
        String rawPassword = "testPassword123";

        // When
        String hashedPassword = PasswordUtils.encodePassword(rawPassword);

        // Then
        assertNotNull(hashedPassword);
        assertNotEquals(rawPassword, hashedPassword);
        assertTrue(hashedPassword.startsWith("$2a$12$") || hashedPassword.startsWith("$2y$12$") || hashedPassword.startsWith("$2b$12$"));
    }

    @Test
    void encodePassword_WithDifferentInputs_ShouldReturnDifferentHashes() {
        // Given
        String password1 = "password1";
        String password2 = "password2";

        // When
        String hash1 = PasswordUtils.encodePassword(password1);
        String hash2 = PasswordUtils.encodePassword(password2);

        // Then
        assertNotEquals(hash1, hash2);
    }

    @Test
    void encodePassword_WithSameInput_ShouldReturnDifferentHashesEachTime() {
        // Given
        String password = "samePassword";

        // When
        String hash1 = PasswordUtils.encodePassword(password);
        String hash2 = PasswordUtils.encodePassword(password);

        // Then
        assertNotEquals(hash1, hash2); // BCrypt generates different salt each time
    }

    @Test
    void checkPassword_WithMatchingPassword_ShouldReturnTrue() {
        // Given
        String rawPassword = "correctPassword";
        String encodedPassword = PasswordUtils.encodePassword(rawPassword);

        // When
        boolean result = PasswordUtils.checkPassword(rawPassword, encodedPassword);

        // Then
        assertTrue(result);
    }

    @Test
    void checkPassword_WithNonMatchingPassword_ShouldReturnFalse() {
        // Given
        String rawPassword = "correctPassword";
        String wrongPassword = "wrongPassword";
        String encodedPassword = PasswordUtils.encodePassword(rawPassword);

        // When
        boolean result = PasswordUtils.checkPassword(wrongPassword, encodedPassword);

        // Then
        assertFalse(result);
    }

    @Test
    void checkPassword_WithNullRawPassword_ShouldReturnFalse() {
        // Given
        String encodedPassword = PasswordUtils.encodePassword("testPassword");

        // When
        boolean result = PasswordUtils.checkPassword(null, encodedPassword);

        // Then
        assertFalse(result);
    }

    @Test
    void checkPassword_WithNullEncodedPassword_ShouldThrowException() {
        // Given
        String rawPassword = "testPassword";

        // When & Then
        assertThrows(NullPointerException.class, () -> {
            PasswordUtils.checkPassword(rawPassword, null);
        });
    }

    @Test
    void checkPassword_WithBothNull_ShouldThrowException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            PasswordUtils.checkPassword(null, null);
        });
    }

    @Test
    void checkPassword_WithEmptyPassword_ShouldWorkCorrectly() {
        // Given
        String rawPassword = "";
        String encodedPassword = PasswordUtils.encodePassword(rawPassword);

        // When
        boolean result = PasswordUtils.checkPassword(rawPassword, encodedPassword);

        // Then
        assertTrue(result);
    }

    @Test
    void checkPassword_WithEmptyAndNonEmpty_ShouldReturnFalse() {
        // Given
        String rawPassword = "";
        String encodedPassword = PasswordUtils.encodePassword("nonEmptyPassword");

        // When
        boolean result = PasswordUtils.checkPassword(rawPassword, encodedPassword);

        // Then
        assertFalse(result);
    }
}
