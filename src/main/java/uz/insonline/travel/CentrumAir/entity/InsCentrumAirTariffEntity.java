package uz.insonline.travel.CentrumAir.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "INS_CENTRUM_AIR_TARIFF")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InsCentrumAirTariffEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ins_centrum_air_tariff_seq_gen")
    @SequenceGenerator(name = "ins_centrum_air_tariff_seq_gen",
            sequenceName = "INS_CENTRUM_AIR_TARIFF_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RISK_ID", nullable = false)
    private InsCentrumAirRiskEntity risk;

    @Column(name = "TARIFF_CODE", length = 50)
    private String tariffCode;

    @Column(name = "ONE_WAY_SUM", precision = 15, scale = 2)
    private BigDecimal oneWaySum;

    @Column(name = "ROUND_TRIP_SUM", precision = 15, scale = 2)
    private BigDecimal roundTripSum;

    @Column(name = "CURRENCY", nullable = false, length = 3)
    private String currency;

    @Column(name = "POLICY_GROUP", nullable = false)
    private Integer policyGroup;
}
