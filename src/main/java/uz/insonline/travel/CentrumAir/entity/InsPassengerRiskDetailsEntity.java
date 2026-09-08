package uz.insonline.travel.CentrumAir.entity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "INS_PASSENGER_RISK_DETAILS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsPassengerRiskDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ins_passenger_risks_seq_gen")
    @SequenceGenerator(
            name = "ins_passenger_risks_seq_gen",
            sequenceName = "SEQ_PASSENGER_RISKS",
            allocationSize = 1
    )
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "ANKETA_ID")
    private Long anketaId;

    @Column(name = "TRAVEL_ID")
    private Long travelId;

    @Column(name = "RISK_CODE", length = 50)
    private String riskCode;

    @Column(name = "PREMIUM", precision = 10, scale = 2)
    private BigDecimal premium;

    @Column(name = "OBJECT_DESCRIPTION", length = 500)
    private String objectDescription;
}