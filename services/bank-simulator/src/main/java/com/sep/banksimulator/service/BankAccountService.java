package com.sep.banksimulator.service;

import com.sep.banksimulator.dto.CheckBalanceRequest;
import com.sep.banksimulator.dto.CheckBalanceResponse;
import com.sep.banksimulator.entity.BankAccount;
import com.sep.banksimulator.entity.BankCard;
import com.sep.banksimulator.repository.BankCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankCardRepository bankCardRepository;

    public CheckBalanceResponse checkBalance(CheckBalanceRequest request) {
        String hashedPan = hashPan(request.getPan());

        log.info("🔍 Looking for card with hashed PAN: {}", hashedPan);

        BankCard card = bankCardRepository.findByPan(hashedPan).orElse(null);

        if (card == null) {
            log.warn("❌ Card NOT FOUND for hash: {}", hashedPan);
            return CheckBalanceResponse.builder()
                    .sufficient(false)
                    .reason("CARD_NOT_FOUND")
                    .build();
        }

        log.info("✅ Card FOUND!");


        BankAccount account = card.getAccount();
        log.info("💰 Account balance: {}, Required: {}", account.getBalance(), request.getAmount());

        boolean hasFunds = account.getBalance() >= request.getAmount();

        return CheckBalanceResponse.builder()
                .sufficient(hasFunds)
                .reason(hasFunds ? null : "INSUFFICIENT_FUNDS")
                .build();
    }

    private String hashPan(String pan) {
        try {
            String cleanPan = pan.replaceAll("\\D", "");
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(cleanPan.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}