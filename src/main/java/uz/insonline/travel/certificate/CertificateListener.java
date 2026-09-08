package uz.insonline.travel.certificate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import uz.insonline.travel.certificate.api.CertificateRequest;

/**
 * Обработчик очереди сертификатов. Ошибка приводит к ретраям Rabbit и, после их исчерпания,
 * к отправке сообщения в DLQ — выпуск полисов при этом уже завершён и не страдает.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CertificateListener {

    private final CertificateService certificateService;

    @RabbitListener(queues = "${certificate.rabbit.queue}", containerFactory = "certificateListenerContainerFactory")
    public void onCertificateRequest(CertificateRequest request) {
        log.info("Certificate request received: booking={}, pnr={}, event={}",
                request.bookingId(), request.pnr(), request.eventId());
        try {
            certificateService.issue(request);
        } catch (RuntimeException e) {
            log.error("Certificate generation failed for booking {}", request.bookingId(), e);
            certificateService.markFailed(request.bookingId(), e.getMessage());
            throw e;
        }
    }
}
