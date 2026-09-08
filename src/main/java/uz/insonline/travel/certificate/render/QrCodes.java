package uz.insonline.travel.certificate.render;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;

/** QR-коды сертификата: лендинг подачи заявления об убытке и контакты ассистанса (ТЗ п. 9.2). */
public final class QrCodes {

    private static final int SIZE = 240;

    private QrCodes() {
    }

    /** PNG в виде data-URI — документ остаётся самодостаточным, без внешних запросов при рендеринге. */
    public static String dataUri(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        try {
            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, SIZE, SIZE,
                    Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M, EncodeHintType.MARGIN, 0));
            ByteArrayOutputStream png = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", png);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(png.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to render QR code", e);
        }
    }
}
