package com.sep.qrpayment.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.sep.qrpayment.dto.GenerateQrRequest;
import com.sep.qrpayment.dto.GenerateQrResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class QrCodeService {

    public GenerateQrResponse generate(GenerateQrRequest request) {
        String qrText = buildPayload(request);
        String base64 = generatePngBase64(qrText, 260, 260);

        return GenerateQrResponse.builder()
                .qrText(qrText)
                .qrImageBase64(base64)
                .build();
    }

    private String buildPayload(GenerateQrRequest r) {
        return "SEPQR|bankPaymentId=" + r.getBankPaymentId()
                + "|pspPaymentId=" + r.getPspPaymentId()
                + "|amount=" + r.getAmount()
                + "|currency=" + r.getCurrency()
                + "|stan=" + r.getStan()
                + "|pspTimestamp=" + r.getPspTimestamp();
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
            throw new IllegalStateException("Failed to generate QR image", e);
        }
    }
}
