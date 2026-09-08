package uz.insonline.travel.certificate.render;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.util.XRLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import uz.insonline.travel.certificate.config.CertificateProperties;
import uz.insonline.travel.certificate.model.CertificateModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Locale;

/** HTML-шаблон сертификата → PDF. Шрифты встроены в документ, внешние ресурсы не запрашиваются. */
@Slf4j
@Component
@RequiredArgsConstructor
public class CertificatePdfRenderer {

    private static final String FONT_FAMILY = "InsonSans";
    private static final String REGULAR_FONT = "fonts/DejaVuSans.ttf";
    private static final String BOLD_FONT = "fonts/DejaVuSans-Bold.ttf";

    static {
        // openhtmltopdf по умолчанию пишет свой лог в java.util.logging мимо логов приложения.
        XRLog.setLoggingEnabled(false);
    }

    private final TemplateEngine templateEngine;
    private final CertificateProperties properties;

    public byte[] render(CertificateModel certificate) {
        Context context = new Context(Locale.forLanguageTag(certificate.language()));
        context.setVariable("certificate", certificate);
        context.setVariable("brand", properties.getBrand());
        context.setVariable("logo", logoDataUri());
        String html = templateEngine.process("certificate/certificate", context);

        try (ByteArrayOutputStream pdf = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder()
                    .useFastMode()
                    .withHtmlContent(html, null)
                    .toStream(pdf);
            builder.useFont(() -> openFont(REGULAR_FONT), FONT_FAMILY, 400, PdfRendererBuilder.FontStyle.NORMAL, true);
            builder.useFont(() -> openFont(BOLD_FONT), FONT_FAMILY, 700, PdfRendererBuilder.FontStyle.NORMAL, true);
            builder.run();
            return pdf.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to render certificate " + certificate.certificateNumber(), e);
        }
    }

    /** Логотип встраивается в документ; если файла нет, шаблон печатает текстовый логотип. */
    private String logoDataUri() {
        ClassPathResource logo = new ClassPathResource(properties.getBrand().getLogoResource());
        if (!logo.exists()) {
            return null;
        }
        try (InputStream stream = logo.getInputStream()) {
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(stream.readAllBytes());
        } catch (IOException e) {
            log.warn("Certificate logo {} cannot be read", properties.getBrand().getLogoResource(), e);
            return null;
        }
    }

    private InputStream openFont(String resource) {
        try {
            return new ClassPathResource(resource).getInputStream();
        } catch (IOException e) {
            throw new IllegalStateException("Certificate font " + resource + " is missing", e);
        }
    }
}
