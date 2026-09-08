package uz.insonline.travel.certificate.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/** Выпущенный сертификат INSON: один на бронь (ТЗ п. 4.3, 9.2). */
@Entity
@Table(name = "INS_INSURANCE_CERTIFICATE")
@Getter
@Setter
@NoArgsConstructor
public class InsCertificateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_ins_certificate_gen")
    @SequenceGenerator(name = "seq_ins_certificate_gen",
            sequenceName = "SEQ_INS_INSURANCE_CERTIFICATE", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "CERTIFICATE_NUMBER", nullable = false, unique = true, length = 50)
    private String certificateNumber;

    @Column(name = "BOOKING_ID", nullable = false, unique = true)
    private Long bookingId;

    @Column(name = "PNR", nullable = false, length = 50)
    private String pnr;

    @Column(name = "LANGUAGE", nullable = false, length = 2)
    private String language;

    /** Ключ объекта в MinIO. */
    @Column(name = "OBJECT_KEY", length = 500)
    private String objectKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private CertificateStatus status;

    @Column(name = "RECIPIENTS", length = 1000)
    private String recipients;

    @Column(name = "TEMPLATE_VERSION", nullable = false, length = 20)
    private String templateVersion;

    @Column(name = "ERROR_MESSAGE", length = 1000)
    private String errorMessage;

    @Column(name = "CREATED_AT", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "SENT_AT")
    private OffsetDateTime sentAt;

    @PrePersist
    void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
