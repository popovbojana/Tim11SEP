package com.sep.cardpayment.util;

import lombok.NoArgsConstructor;

import java.time.YearMonth;

@NoArgsConstructor
public final class ExpiryUtil {

    public static YearMonth parseMmYy(String expiry) {
        if (expiry == null) return null;
        String v = expiry.trim();

        if (!v.matches("\\d{2}/\\d{2}")) return null;

        int mm = Integer.parseInt(v.substring(0, 2));
        int yy = Integer.parseInt(v.substring(3, 5));

        if (mm < 1 || mm > 12) return null;

        int year = 2000 + yy;
        return YearMonth.of(year, mm);
    }

    public static boolean isExpired(YearMonth exp) {
        if (exp == null) return true;
        YearMonth now = YearMonth.now();
        return exp.isBefore(now);
    }
}
