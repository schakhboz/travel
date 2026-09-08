package uz.insonline.travel.certificate.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/** Доступ к объектному хранилищу MinIO, в котором лежат сгенерированные сертификаты. */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private String endpoint = "http://localhost:9000";
    private String accessKey;
    private String secretKey;
    private String bucket = "insurance-certificates";

    /** Создавать бакет при старте, если его нет. */
    private boolean createBucket = true;

    /** Срок жизни ссылки на скачивание сертификата. */
    private Duration linkExpiry = Duration.ofDays(7);
}
