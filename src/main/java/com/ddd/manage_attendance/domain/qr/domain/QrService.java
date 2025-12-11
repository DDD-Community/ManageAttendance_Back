package com.ddd.manage_attendance.domain.qr.domain;

import com.ddd.manage_attendance.core.util.QrUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;

@Service
public class QrService {
    private static final String FORMAT = "PNG";
    private static final String PREFIX = "DDD";

    public byte[] generateQrCodeImage(String content, int width, int height) {
        try {
            final BitMatrix matrix =
                    new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height);

            final BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, FORMAT, baos);

            return baos.toByteArray();
        } catch (WriterException | IOException e) {
            throw new RuntimeException("QR 코드 생성 실패", e);
        }
    }

    public String generateQrBase64(String content, int width, int height) {
        final byte[] image = generateQrCodeImage(content, width, height);
        return QrUtil.toBase64(image);
    }

    public String generateQrCodeKey() {
        final String token = UUID.randomUUID().toString().replace("-", "");

        return PREFIX + "|" + token;
    }

    public void extractTokenIfValid(String qrValue) {
        final String[] parts = qrValue.split("\\|");
        if (parts.length < 2) {
            throw new IllegalArgumentException("QR 형식이 올바르지 않습니다.");
        }
        final String prefix = parts[0];
        if (!PREFIX.equals(prefix)) {
            throw new IllegalArgumentException("유효한 서비스 QR이 아닙니다.");
        }
    }
}
