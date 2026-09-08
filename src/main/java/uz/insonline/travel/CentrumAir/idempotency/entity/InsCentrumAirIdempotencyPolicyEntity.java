package uz.insonline.travel.CentrumAir.idempotency.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/** Успешно выпущенный полис заявки: позволяет повтору довыпустить только недостающие (ТЗ п. 7.3). */
@Entity
@Table(name = "INS_CENTRUM_AIR_IDEMP_POLICY")
@Getter
@Setter
@NoArgsConstructor
public class InsCentrumAirIdempotencyPolicyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_ca_idemp_policy_gen")
    @SequenceGenerator(name = "seq_ca_idemp_policy_gen",
            sequenceName = "SEQ_INS_CA_IDEMP_POLICY", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "IDEMPOTENCY_ID", nullable = false)
    private Long idempotencyId;

    @Column(name = "POLICY_GROUP", nullable = false)
    private Integer policyGroup;

    @Column(name = "CONTRACT_ID", nullable = false)
    private Long contractId;

    @Column(name = "POLICY_ID", nullable = false)
    private Long policyId;

    @Column(name = "CREATED_AT", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}
