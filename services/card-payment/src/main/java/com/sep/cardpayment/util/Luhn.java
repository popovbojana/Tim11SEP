package com.sep.cardpayment.util;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class Luhn {

    public static boolean isValid(String pan) {
        if (pan == null) return false;

        String digits = pan.replaceAll("\\D", "");
        if (!digits.matches("\\d{12,19}")) return false;

        int sum = 0;
        boolean doubleIt = false;

        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = digits.charAt(i) - '0';
            if (doubleIt) {
                d *= 2;
                if (d > 9) d -= 9;
            }
            sum += d;
            doubleIt = !doubleIt;
        }
        return sum % 10 == 0;
    }
}
