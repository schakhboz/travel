package uz.insonline.travel.CentrumAir.idempotency.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.insonline.travel.CentrumAir.idempotency.IdempotencyStatus;

import java.time.OffsetDateTime;

/** Заявка на выпуск, идентифицируемая ключом идемпотентности (ТЗ п. 7.3). */
@Entity
@Table(name = "INS_CENTRUM_AIR_IDEMPOTENCY")
@Getter
@Setter
@NoArgsConstructor
public class InsCentrumAirIdempotencyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_ca_idempotency_gen")
    @SequenceGenerator(name = "seq_ca_idempotency_gen",
            sequenceName = "SEQ_INS_CA_IDEMPOTENCY", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "IDEMPOTENCY_KEY", nullable = false, unique = true, length = 255)
    private String idempotencyKey;

    @Column(name = "PNR", nullable = false, length = 50)
    private String pnr;

    /** Отпечаток набора продуктов: по нему ловятся дубли выпуска на ту же бронь (ТЗ п. 7.6.5). */
    @Column(name = "PRODUCT_FINGERPRINT", nullable = false, length = 255)
    private String productFingerprint;

    /** Хэш тела запроса: тот же ключ с другим телом — ошибка, а не повтор. */
    @Column(name = "REQUEST_HASH", nullable = false, length = 64)
    private String requestHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private IdempotencyStatus status;

    @Column(name = "BOOKING_ID")
    private Long bookingId;

    @Lob
    @Column(name = "RESPONSE_JSON")
    private String responseJson;

    @Column(name = "ERROR_MESSAGE", length = 1000)
    private String errorMessage;

    @Column(name = "CREATED_AT", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private OffsetDateTime updatedAt;

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
