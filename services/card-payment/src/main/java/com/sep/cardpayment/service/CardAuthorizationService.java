package com.sep.cardpayment.service;

import com.sep.cardpayment.dto.*;
import com.sep.cardpayment.util.ExpiryUtil;
import com.sep.cardpayment.util.Luhn;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.YearMonth;
import java.util.Set;
import java.util.UUID;

@Service
public class CardAuthorizationService {

    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("€");

    public AuthorizeCardPaymentResponse authorize(AuthorizeCardPaymentRequest req) {

        if (req.getCurrency() == null || !SUPPORTED_CURRENCIES.contains(req.getCurrency().trim().toUpperCase())) {
            return failed(FailureReason.CURRENCY_NOT_SUPPORTED);
        }

        String holder = req.getCardHolderName() == null ? "" : req.getCardHolderName().trim();
        if (holder.isBlank()) {
            return failed(FailureReason.INVALID_HOLDER_NAME);
        }

        if (!Luhn.isValid(req.getPan())) {
            return failed(FailureReason.INVALID_PAN);
        }

        YearMonth exp = ExpiryUtil.parseMmYy(req.getExpiry());
        if (exp == null) {
            return failed(FailureReason.INVALID_EXPIRY_FORMAT);
        }
        if (ExpiryUtil.isExpired(exp)) {
            return failed(FailureReason.EXPIRED_CARD);
        }

        String cvv = req.getSecurityCode() == null ? "" : req.getSecurityCode().trim();
        if (!cvv.matches("\\d{3,4}")) {
            return failed(FailureReason.INVALID_CVV);
        }

        return AuthorizeCardPaymentResponse.builder()
                .status(CardPaymentStatus.SUCCESS)
                .reason(null)
                .globalTransactionId(UUID.randomUUID().toString())
                .acquirerTimestamp(Instant.now())
                .build();
    }

    private AuthorizeCardPaymentResponse failed(FailureReason reason) {
        return AuthorizeCardPaymentResponse.builder()
                .status(CardPaymentStatus.FAILED)
                .reason(reason)
                .globalTransactionId(UUID.randomUUID().toString())
                .acquirerTimestamp(Instant.now())
                .build();
    }
}
