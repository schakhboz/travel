package uz.insonline.travel.CentrumAir.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "INS_CENTRUM_AIR_RISK_V2")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InsCentrumAirRiskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ins_centrum_air_seq_gen")
    @SequenceGenerator(name = "ins_centrum_air_seq_gen",
            sequenceName = "INS_CENTRUM_AIR_RISK_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "RISK_CODE", nullable = false, unique = true, length = 50)
    private String riskCode;

    @Column(name = "TITLE", nullable = false, length = 250)
    private String title;

    @Column(name = "CLASS_ID", nullable = false)
    private String classId;

    @Column(name = "INSURANCE_SUM", nullable = false, precision = 15, scale = 2)
    private BigDecimal insuranceSum;

    @Column(name = "CURRENCY", nullable = false, length = 3)
    private String currency;
}
