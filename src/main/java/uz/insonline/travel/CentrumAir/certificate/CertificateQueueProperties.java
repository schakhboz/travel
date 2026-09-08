package uz.insonline.travel.CentrumAir.certificate;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Куда публиковать заявки на сертификат. Очередь и её обработчик живут в сервисе документов. */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "certificate.queue")
public class CertificateQueueProperties {

    private String exchange = "insurance.certificate.exchange";
    private String routingKey = "insurance.certificate.issue";
}
