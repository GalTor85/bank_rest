package com.bankrest.service;

import com.bankrest.util.EncryptionUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionUtilTest {

    private final EncryptionUtil util = new EncryptionUtil();

    @Test
    void encrypt_ShouldReturnNonEmptyString() {
        String result = util.encrypt("1234567812345678");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void encrypt_ShouldReturnDifferentValueThanInput() {
        String original = "1234567812345678";
        String encrypted = util.encrypt(original);

        assertNotEquals(original, encrypted);
    }

    @Test
    void encrypt_ShouldReturnDifferentValues_ForDifferentInputs() {
        String encrypted1 = util.encrypt("1111111111111111");
        String encrypted2 = util.encrypt("2222222222222222");

        assertNotEquals(encrypted1, encrypted2);
    }

    @Test
    void encrypt_ShouldBeDeterministic() {
        String input = "1234567812345678";
        String result1 = util.encrypt(input);
        String result2 = util.encrypt(input);

        assertEquals(result1, result2);
    }
}