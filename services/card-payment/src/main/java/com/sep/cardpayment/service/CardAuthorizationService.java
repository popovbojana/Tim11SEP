package com.sep.cardpayment.service;

import com.sep.cardpayment.dto.AuthorizeCardPaymentRequest;
import com.sep.cardpayment.dto.AuthorizeCardPaymentResponse;
import com.sep.cardpayment.dto.CheckBalanceRequest;
import com.sep.cardpayment.dto.CheckBalanceResponse;
import com.sep.cardpayment.enums.CardBrand;
import com.sep.cardpayment.enums.CardPaymentStatus;
import com.sep.cardpayment.enums.FailureReason;
import com.sep.cardpayment.util.CardBrandDetector;
import com.sep.cardpayment.util.ExpiryUtil;
import com.sep.cardpayment.util.Luhn;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.YearMonth;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardAuthorizationService {

    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("€", "EUR");
    private static final String BANK_BALANCE_CHECK_URL = "http://localhost:8084/api/accounts/check-balance";

    private final RestTemplate restTemplate;

    public AuthorizeCardPaymentResponse authorize(AuthorizeCardPaymentRequest req) {

        String currency = req.getCurrency() == null ? "" : req.getCurrency().trim().toUpperCase();
        if (currency.isBlank() || !SUPPORTED_CURRENCIES.contains(currency)) {
            return failed(FailureReason.CURRENCY_NOT_SUPPORTED);
        }

        String holder = req.getCardHolderName() == null ? "" : req.getCardHolderName().trim();
        if (holder.isBlank()) {
            return failed(FailureReason.INVALID_HOLDER_NAME);
        }

        String panDigits = req.getPan() == null ? "" : req.getPan().replaceAll("\\D", "");
        if (!Luhn.isValid(panDigits)) {
            return failed(FailureReason.INVALID_PAN);
        }

        CardBrand brand = CardBrandDetector.detect(panDigits);
        if (brand == null) {
            return failed(FailureReason.UNSUPPORTED_CARD_BRAND);
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

        if (!checkBalance(req.getPan(), cvv, req.getAmount())) {
            return failed(FailureReason.INSUFFICIENT_FUNDS);
        }

        return AuthorizeCardPaymentResponse.builder()
                .status(CardPaymentStatus.SUCCESS)
                .reason(null)
                .globalTransactionId(UUID.randomUUID().toString())
                .acquirerTimestamp(Instant.now())
                .build();
    }

    private boolean checkBalance(String pan, String cvv, double amount) {
        try {
            // Očisti PAN pre slanja (ukloni razmake i crtice)
            String cleanPan = pan == null ? "" : pan.replaceAll("\\D", "");

            CheckBalanceRequest request = CheckBalanceRequest.builder()
                    .pan(cleanPan)  // ⬅️ šalji čist PAN
                    .cvv(cvv)
                    .amount(amount)
                    .build();

            CheckBalanceResponse response = restTemplate.postForObject(
                    BANK_BALANCE_CHECK_URL,
                    request,
                    CheckBalanceResponse.class
            );

            return response != null && response.isSufficient();

        } catch (Exception e) {
            return false;
        }
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