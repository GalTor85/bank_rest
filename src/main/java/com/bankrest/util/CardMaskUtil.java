package com.bankrest.util;

import org.springframework.stereotype.Component;

@Component
public class CardMaskUtil {

    private static final String MASK_PATTERN = "**** **** **** ";

    public String mask(String lastFourDigits) {
        if (lastFourDigits == null || lastFourDigits.length() < 4) {
            return "**** **** **** ****";
        }
        String last4 = lastFourDigits.substring(lastFourDigits.length() - 4);
        return MASK_PATTERN + last4;
    }
}