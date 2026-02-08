package com.sep.banksimulator.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class HashUtils {

    private static final String SALT = "pci-dss-bank-simulator-salt-2026";

    public static String hashPan(String pan) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String saltedPan = pan + SALT;
            byte[] hash = digest.digest(saltedPan.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error: Hashing algorithm SHA-256 not found", e);
        }
    }
}