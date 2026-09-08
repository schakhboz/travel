package uz.insonline.travel.certificate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CertificateServiceTest {

    private static final byte[] PDF = "%PDF-fake".getBytes();

    @Mock
    private InsCertificateRepository certificateRepository;
    @Mock
    private CertificateNumberGenerator numberGenerator;
    @Mock
    private CertificateModelFactory modelFactory;
    @Mock
    private CertificatePdfRenderer renderer;
    @Mock
    private MinioStorageService storage;
    @Mock
    private CertificateMailer mailer;
    @Spy
    private CertificateProperties properties = new CertificateProperties();

    @InjectMocks
    private CertificateService service;

    @Test
    void generatesStoresAndSendsCertificate() {
        when(certificateRepository.findByBookingId(42L)).thenReturn(Optional.empty());
        when(numberGenerator.next(any())).thenReturn("CERT-2026-00000001");
        when(certificateRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(modelFactory.build(any(), eq("CERT-2026-00000001"))).thenReturn(model("CERT-2026-00000001"));
        when(renderer.render(any())).thenReturn(PDF);

        service.issue(request(false));

        verify(storage).upload(contains("CERT-2026-00000001.pdf"), eq(PDF), eq("application/pdf"));
        verify(mailer).send(eq(List.of("passenger@example.com")), any(), eq(PDF));
        verify(certificateRepository, atLeastOnce()).save(argThat(saved ->
                saved.getStatus() == CertificateStatus.SENT && saved.getSentAt() != null));
    }

    @Test
    void redeliveryOfSentCertificateChangesNothing() {
        when(certificateRepository.findByBookingId(42L)).thenReturn(Optional.of(stored(CertificateStatus.SENT)));

        service.issue(request(false));

        verifyNoInteractions(renderer, storage, mailer);
    }

    @Test
    void resendReusesStoredPdfAndDoesNotRenderAgain() {
        when(certificateRepository.findByBookingId(42L)).thenReturn(Optional.of(stored(CertificateStatus.SENT)));
        when(modelFactory.build(any(), eq("CERT-2026-00000001"))).thenReturn(model("CERT-2026-00000001"));
        when(storage.download("certificates/2026/07/CERT-2026-00000001.pdf")).thenReturn(PDF);

        service.issue(request(true));

        verify(renderer, never()).render(any());
        verify(storage, never()).upload(anyString(), any(), anyString());
        verify(mailer).send(eq(List.of("passenger@example.com")), any(), eq(PDF));
    }

    private static InsCertificateEntity stored(CertificateStatus status) {
        InsCertificateEntity entity = new InsCertificateEntity();
        entity.setId(1L);
        entity.setCertificateNumber("CERT-2026-00000001");
        entity.setBookingId(42L);
        entity.setStatus(status);
        entity.setObjectKey("certificates/2026/07/CERT-2026-00000001.pdf");
        return entity;
    }

    private static CertificateModel model(String number) {
        return new CertificateModel(number, "30.07.2026", "PNR123", "RU", "1.0", List.of(), List.of(), null);
    }

    private static CertificateRequest request(boolean resend) {
        return new CertificateRequest("event-1", 42L, "PNR123", "RU",
                LocalDate.of(2026, 7, 30), LocalDate.of(2026, 7, 30), LocalDate.of(2026, 8, 1),
                false, "Ivan Ivanov", Map.of("STANDARD", 0), "STANDARD",
                List.of(new CertificateRequest.InsuredPerson("Ivanov Ivan", "AB-1111111",
                        LocalDate.of(1995, 8, 26), "passenger@example.com")),
                List.of(), resend);
    }
}
