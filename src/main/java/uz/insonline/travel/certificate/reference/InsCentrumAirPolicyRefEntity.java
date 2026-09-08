package uz.insonline.travel.certificate.reference;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "INS_CENTRUM_AIR_POLICY_REF")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsCentrumAirPolicyRefEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_policy_ref_gen")
    @SequenceGenerator(
            name = "seq_policy_ref_gen",
            sequenceName = "SEQ_INS_CENTRUM_AIR_POLICY_REF",
            allocationSize = 1
    )
    @Column(name = "ID")
    private Long id;

    @Column(name = "RISK_CODE", nullable = false, length = 255)
    private String riskCode;

    @Column(name = "PRODUCT_NAME", nullable = false, length = 500)
    private String productName;

    @Lob
    @Column(name = "INSURANCE_RISKS")
    private String insuranceRisks;

    @Lob
    @Column(name = "INSURANCE_OBJECT")
    private String insuranceObject;

    @Lob
    @Column(name = "TERRITORY")
    private String territory;
}
