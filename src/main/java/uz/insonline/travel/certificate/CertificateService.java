package uz.insonline.travel.certificate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uz.insonline.travel.certificate.api.CertificateRequest;
import uz.insonline.travel.certificate.config.CertificateProperties;
import uz.insonline.travel.certificate.entity.CertificateStatus;
import uz.insonline.travel.certificate.entity.InsCertificateEntity;
import uz.insonline.travel.certificate.mail.CertificateMailer;
import uz.insonline.travel.certificate.model.CertificateModel;
import uz.insonline.travel.certificate.render.CertificatePdfRenderer;
import uz.insonline.travel.certificate.repository.InsCertificateRepository;
import uz.insonline.travel.certificate.storage.MinioStorageService;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Выпуск сертификата INSON: сборка модели, генерация PDF, сохранение в MinIO и письмо пассажиру.
 * <p>
 * Сертификат один на бронь: повторная доставка сообщения не создаёт второй документ, а
 * повторная отправка (resend) переиспользует уже сохранённый PDF.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {

    private static final DateTimeFormatter KEY_DATE = DateTimeFormatter.ofPattern("yyyy/MM");

    private final InsCertificateRepository certificateRepository;
    private final CertificateNumberGenerator numberGenerator;
    private final CertificateModelFactory modelFactory;
    private final CertificatePdfRenderer renderer;
    private final MinioStorageService storage;
    private final CertificateMailer mailer;
    private final CertificateProperties properties;

    @Transactional
    public void issue(CertificateRequest request) {
        Optional<InsCertificateEntity> existing = certificateRepository.findByBookingId(request.bookingId());

        if (existing.filter(entity -> entity.getStatus() == CertificateStatus.SENT).isPresent()
                && !request.resend()) {
            log.info("Certificate for booking {} is already sent, message skipped", request.bookingId());
            return;
        }

        InsCertificateEntity certificate = existing.orElseGet(() -> newCertificate(request));
        CertificateModel model = modelFactory.build(request, certificate.getCertificateNumber());

        byte[] pdf = storedPdf(certificate).orElseGet(() -> {
            byte[] rendered = renderer.render(model);
            String objectKey = objectKey(request.issueDate(), certificate.getCertificateNumber());
            storage.upload(objectKey, rendered, "application/pdf");
            certificate.setObjectKey(objectKey);
            certificate.setStatus(CertificateStatus.STORED);
            certificateRepository.saveAndFlush(certificate);
            return rendered;
        });

        List<String> recipients = recipients(request);
        mailer.send(recipients, model, pdf);

        certificate.setRecipients(String.join(",", recipients));
        certificate.setStatus(CertificateStatus.SENT);
        certificate.setSentAt(OffsetDateTime.now());
        certificate.setErrorMessage(null);
        certificateRepository.save(certificate);
    }

    /** Отметка о неуспехе пишется отдельной транзакцией: основная к этому моменту откатывается. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long bookingId, String errorMessage) {
        certificateRepository.findByBookingId(bookingId).ifPresent(certificate -> {
            certificate.setStatus(CertificateStatus.FAILED);
            certificate.setErrorMessage(errorMessage == null ? null
                    : errorMessage.substring(0, Math.min(errorMessage.length(), 1000)));
            certificateRepository.save(certificate);
        });
    }

    private InsCertificateEntity newCertificate(CertificateRequest request) {
        InsCertificateEntity certificate = new InsCertificateEntity();
        certificate.setCertificateNumber(numberGenerator.next(request.issueDate()));
        certificate.setBookingId(request.bookingId());
        certificate.setPnr(request.pnr());
        certificate.setLanguage(request.language());
        certificate.setTemplateVersion(properties.getTemplateVersion());
        certificate.setStatus(CertificateStatus.PENDING);
        return certificateRepository.saveAndFlush(certificate);
    }

    private Optional<byte[]> storedPdf(InsCertificateEntity certificate) {
        if (certificate.getObjectKey() == null) {
            return Optional.empty();
        }
        log.info("Reusing stored certificate {}", certificate.getCertificateNumber());
        return Optional.of(storage.download(certificate.getObjectKey()));
    }

    private static String objectKey(LocalDate issueDate, String certificateNumber) {
        LocalDate date = issueDate != null ? issueDate : LocalDate.now();
        return "certificates/%s/%s.pdf".formatted(date.format(KEY_DATE), certificateNumber);
    }

    private static List<String> recipients(CertificateRequest request) {
        return request.insured().stream()
                .map(CertificateRequest.InsuredPerson::email)
                .filter(email -> email != null && !email.isBlank())
                .distinct()
                .toList();
    }
}
