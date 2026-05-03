package com.bankrest.service;

import com.bankrest.util.CardMaskUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardMaskUtilTest {

    private final CardMaskUtil util = new CardMaskUtil();

    @Test
    void mask_ShouldReturnMaskedNumber() {
        String result = util.mask("1234");
        assertEquals("**** **** **** 1234", result);
    }

    @Test
    void mask_ShouldHandleLongerString() {
        String result = util.mask("abcdef1234");
        assertEquals("**** **** **** 1234", result);
    }

    @Test
    void mask_ShouldReturnAllAsterisks_WhenNull() {
        String result = util.mask(null);
        assertEquals("**** **** **** ****", result);
    }

    @Test
    void mask_ShouldReturnAllAsterisks_WhenTooShort() {
        String result = util.mask("12");
        assertEquals("**** **** **** ****", result);
    }
}