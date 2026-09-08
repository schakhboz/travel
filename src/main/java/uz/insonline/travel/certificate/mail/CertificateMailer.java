package uz.insonline.travel.certificate.mail;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import uz.insonline.travel.certificate.config.CertificateProperties;
import uz.insonline.travel.certificate.model.CertificateModel;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/**
 * Письмо пассажиру «Ваш страховой полис» с вложенным сертификатом INSON (ТЗ п. 9.3).
 * Полис ЕАИС пассажиру не направляется.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CertificateMailer {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final CertificateProperties properties;

    public void send(List<String> recipients, CertificateModel certificate, byte[] pdf) {
        if (recipients.isEmpty()) {
            log.warn("Certificate {} has no recipients, email skipped", certificate.certificateNumber());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(properties.getMail().getFrom());
            helper.setTo(recipients.toArray(String[]::new));
            helper.setSubject(properties.getMail().getSubject());
            helper.setText(body(certificate), true);
            helper.addAttachment(
                    properties.getMail().getFileNamePattern().formatted(certificate.certificateNumber()),
                    new ByteArrayResource(pdf), "application/pdf");

            mailSender.send(message);
            log.info("Certificate {} sent to {}", certificate.certificateNumber(), recipients);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to send certificate " + certificate.certificateNumber(), e);
        }
    }

    private String body(CertificateModel certificate) {
        Context context = new Context(Locale.forLanguageTag(certificate.language()));
        context.setVariable("certificate", certificate);
        context.setVariable("brand", properties.getBrand());
        return templateEngine.process("certificate/email", context);
    }
}
