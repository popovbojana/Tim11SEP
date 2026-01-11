package com.sep.cardpayment.util;

import com.sep.cardpayment.enums.CardBrand;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class CardBrandDetector {

    public static CardBrand detect(String panRaw) {
        if (panRaw == null) return null;

        String pan = panRaw.replaceAll("\\D", "");
        if (pan.isBlank()) return null;

        if (pan.startsWith("4") && pan.length() >= 13 && pan.length() <= 19) {
            return CardBrand.VISA;
        }

        if (pan.length() == 16) {
            int prefix2 = Integer.parseInt(pan.substring(0, 2));
            if (prefix2 >= 51 && prefix2 <= 55) {
                return CardBrand.MASTERCARD;
            }

            int prefix4 = Integer.parseInt(pan.substring(0, 4));
            if (prefix4 >= 2221 && prefix4 <= 2720) {
                return CardBrand.MASTERCARD;
            }
        }

        return null;
    }
}
