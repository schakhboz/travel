package uz.insonline.travel.certificate.storage;

import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.insonline.travel.certificate.config.MinioProperties;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/** Хранение сертификатов в MinIO: загрузка, чтение и временная ссылка на скачивание. */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    @PostConstruct
    void ensureBucket() {
        if (!properties.isCreateBucket()) {
            return;
        }
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(properties.getBucket()).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(properties.getBucket()).build());
                log.info("MinIO bucket {} created", properties.getBucket());
            }
        } catch (Exception e) {
            // Хранилище может подняться позже приложения — падать на старте из-за этого нельзя.
            log.error("MinIO bucket {} is not available at startup", properties.getBucket(), e);
        }
    }

    public void upload(String objectKey, byte[] content, String contentType) {
        try (InputStream stream = new ByteArrayInputStream(content)) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .stream(stream, content.length, -1)
                    .contentType(contentType)
                    .build());
            log.info("Stored {} ({} bytes) in bucket {}", objectKey, content.length, properties.getBucket());
        } catch (Exception e) {
            throw new StorageException("Failed to store " + objectKey, e);
        }
    }

    public byte[] download(String objectKey) {
        try (InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(properties.getBucket())
                .object(objectKey)
                .build())) {
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new StorageException("Failed to read " + objectKey, e);
        }
    }

    /** Временная ссылка на документ — для писем, личного кабинета и поддержки. */
    public String presignedUrl(String objectKey) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .expiry((int) properties.getLinkExpiry().toSeconds(), TimeUnit.SECONDS)
                    .build());
        } catch (Exception e) {
            throw new StorageException("Failed to create link for " + objectKey, e);
        }
    }

    public static class StorageException extends RuntimeException {
        public StorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
