package com.sep.qrpayment.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.sep.qrpayment.dto.GenerateQrRequest;
import com.sep.qrpayment.dto.GenerateQrResponse;
import com.sep.qrpayment.dto.ValidateQrRequest;
import com.sep.qrpayment.dto.ValidateQrResponse;
import com.sep.qrpayment.exception.BadRequestException;
import com.sep.qrpayment.service.QrCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class QrCodeServiceImpl implements QrCodeService {

    @Override
    public GenerateQrResponse generate(GenerateQrRequest request) {
        log.info("📨 Generating QR code — receiver: {}, amount: {} {}", request.getReceiverName(), request.getAmount(), request.getCurrency());

        String qrText = buildIpsPayload(request);
        log.info("⚙️ IPS payload built: {}", qrText);

        String base64 = generatePngBase64(qrText, 260, 260);
        log.info("✅ QR image generated (260x260)");

        return GenerateQrResponse.builder()
                .qrText(qrText)
                .qrImageBase64(base64)
                .build();
    }

    @Override
    public ValidateQrResponse validate(ValidateQrRequest request) {
        log.info("🔍 Validating QR code — receiver: {}, amount: {} {}", request.getReceiverName(), request.getAmount(), request.getCurrency());

        if (request.getCreatedAt() != null) {
            java.time.Duration age = java.time.Duration.between(request.getCreatedAt(), java.time.Instant.now());
            if (age.toMinutes() > 15) {
                log.warn("❌ QR code expired — age: {} minutes", age.toMinutes());
                return ValidateQrResponse.builder().valid(false).reason("QR code expired").build();
            }
        }

        if (request.getQrText() == null || request.getQrText().isBlank()) {
            log.warn("❌ Empty QR text");
            return ValidateQrResponse.builder().valid(false).reason("Empty QR").build();
        }

        Map<String, String> parsed;
        try {
            parsed = parseIpsPayload(request.getQrText());
        } catch (Exception e) {
            log.warn("❌ Invalid IPS format: {}", e.getMessage());
            return ValidateQrResponse.builder().valid(false).reason("Invalid IPS format").build();
        }

        String k = parsed.get("K");
        String v = parsed.get("V");
        String c = parsed.get("C");
        String r = parsed.get("R");
        String n = parsed.get("N");
        String i = parsed.get("I");
        String sf = parsed.get("SF");
        String s = parsed.get("S");
        String ro = parsed.get("RO");

        if (!"PR".equals(k)) {
            log.warn("❌ Validation failed — K mismatch: {}", k);
            return ValidateQrResponse.builder().valid(false).reason("K mismatch").build();
        }
        if (!"01".equals(v)) {
            log.warn("❌ Validation failed — V mismatch: {}", v);
            return ValidateQrResponse.builder().valid(false).reason("V mismatch").build();
        }
        if (!"1".equals(c)) {
            log.warn("❌ Validation failed — C mismatch: {}", c);
            return ValidateQrResponse.builder().valid(false).reason("C mismatch").build();
        }

        if (r == null || r.isBlank()) { log.warn("❌ Missing R tag"); return ValidateQrResponse.builder().valid(false).reason("Missing R").build(); }
        if (n == null || n.isBlank()) { log.warn("❌ Missing N tag"); return ValidateQrResponse.builder().valid(false).reason("Missing N").build(); }
        if (i == null || i.isBlank()) { log.warn("❌ Missing I tag"); return ValidateQrResponse.builder().valid(false).reason("Missing I").build(); }

        if (!isDigitsOnly(r) || r.length() != 18) {
            log.warn("❌ Invalid R (receiver account): {}", r);
            return ValidateQrResponse.builder().valid(false).reason("Invalid R").build();
        }

        if (!r.equals(normalizeAccount(request.getReceiverAccount()))) {
            log.warn("❌ Receiver account mismatch — QR: {}, expected: {}", r, normalizeAccount(request.getReceiverAccount()));
            return ValidateQrResponse.builder().valid(false).reason("Receiver account mismatch").build();
        }

        if (!n.equals(normalizeText(request.getReceiverName()))) {
            log.warn("❌ Receiver name mismatch — QR: '{}', expected: '{}'", n, normalizeText(request.getReceiverName()));
            return ValidateQrResponse.builder().valid(false).reason("Receiver name mismatch").build();
        }

        String expectedI = buildTagI(request.getCurrency(), request.getAmount());
        if (!expectedI.equals(i)) {
            log.warn("❌ Amount/currency mismatch — QR: '{}', expected: '{}'", i, expectedI);
            return ValidateQrResponse.builder().valid(false).reason("Amount/currency mismatch").build();
        }

        if (request.getPaymentCode() != null && !request.getPaymentCode().isBlank()) {
            if (!request.getPaymentCode().equals(sf)) {
                log.warn("❌ SF mismatch — QR: '{}', expected: '{}'", sf, request.getPaymentCode());
                return ValidateQrResponse.builder().valid(false).reason("SF mismatch").build();
            }
        }

        if (request.getPurpose() != null && !request.getPurpose().isBlank()) {
            if (!request.getPurpose().equals(s)) {
                log.warn("❌ S mismatch — QR: '{}', expected: '{}'", s, request.getPurpose());
                return ValidateQrResponse.builder().valid(false).reason("S mismatch").build();
            }
        }

        if (request.getReferenceNumber() != null && !request.getReferenceNumber().isBlank()) {
            if (!request.getReferenceNumber().equals(ro)) {
                log.warn("❌ RO mismatch — QR: '{}', expected: '{}'", ro, request.getReferenceNumber());
                return ValidateQrResponse.builder().valid(false).reason("RO mismatch").build();
            }
        }

        log.info("✅ QR validation passed");
        return ValidateQrResponse.builder().valid(true).reason(null).build();
    }

    private String buildIpsPayload(GenerateQrRequest r) {
        String receiverAcc = normalizeAccount(r.getReceiverAccount());
        String receiverName = normalizeText(r.getReceiverName());
        String purpose = r.getPurpose() == null ? null : normalizeText(r.getPurpose());
        String paymentCode = r.getPaymentCode() == null ? null : r.getPaymentCode().trim();
        String reference = r.getReferenceNumber() == null ? null : normalizeText(r.getReferenceNumber());

        StringBuilder sb = new StringBuilder();
        sb.append("K:PR");
        sb.append("|V:01");
        sb.append("|C:1");
        sb.append("|R:").append(receiverAcc);
        sb.append("|N:").append(receiverName);
        sb.append("|I:").append(buildTagI(r.getCurrency(), r.getAmount()));

        if (paymentCode != null && !paymentCode.isBlank()) sb.append("|SF:").append(paymentCode);
        if (purpose != null && !purpose.isBlank()) sb.append("|S:").append(purpose);
        if (reference != null && !reference.isBlank()) sb.append("|RO:").append(reference);

        return sb.toString();
    }

    private String buildTagI(String currency, BigDecimal amount) {
        BigDecimal finalAmount = (amount == null ? BigDecimal.ZERO : amount);

        if ("EUR".equalsIgnoreCase(currency)) {
            finalAmount = finalAmount.multiply(new BigDecimal("117"));
        }

        BigDecimal bd = finalAmount.setScale(2, RoundingMode.HALF_UP);
        String num = bd.toPlainString().replace('.', ',');

        return "RSD" + num;
    }

    private Map<String, String> parseIpsPayload(String text) {
        String[] parts = text.split("\\|");
        Map<String, String> map = new HashMap<>();

        for (String part : parts) {
            int idx = part.indexOf(':');
            if (idx <= 0) continue;
            String tag = part.substring(0, idx).trim();
            String val = part.substring(idx + 1);
            map.put(tag, val);
        }

        if (!map.containsKey("K") || !map.containsKey("V") || !map.containsKey("C")) {
            throw new BadRequestException("Missing header tags.");
        }

        return map;
    }

    private String normalizeAccount(String acc) {
        return onlyDigits(acc);
    }

    private String onlyDigits(String s) {
        if (s == null) return "";
        return s.replaceAll("\\D", "");
    }

    private String normalizeText(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s+", " ");
    }

    private String generatePngBase64(String text, int width, int height) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(new String(text.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8), BarcodeFormat.QR_CODE, width, height);

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (WriterException | java.io.IOException e) {
            log.error("❌ Failed to generate QR image: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to generate QR image", e);
        }
    }

    private boolean isDigitsOnly(String s) {
        return s != null && !s.isBlank() && s.matches("\\d+");
    }
}