package uz.insonline.travel.CentrumAir.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "INS_ANKETA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsAnketaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ins_anketa_seq_gen")
    @SequenceGenerator(
            name = "ins_anketa_seq_gen",
            sequenceName = "Ins_Anketa_Seq",
            allocationSize = 1
    )
    @Column(name = "INS_ID", nullable = false)
    private Long insId;

    @Column(name = "INS_TYPE", nullable = false)
    private Long insType;

    @Column(name = "OWNER")
    private Long owner;

    @Column(name = "INS_OTV")
    private BigDecimal insOtv;

    @Column(name = "INS_PREM")
    private BigDecimal insPrem;

    @Column(name = "INS_DATEF", nullable = false)
    private LocalDate insDatef;

    @Column(name = "INS_DATET", nullable = false)
    private LocalDate insDatet;

    @Column(name = "INS_DAY")
    private Integer insDay;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "INS_DIV", nullable = false)
    private Long insDiv;

    @Column(name = "VAL_TYPE", nullable = false)
    private Long valType;

    @Column(name = "VAL_KURS", precision = 12, scale = 2)
    private BigDecimal valKurs;

    @Column(name = "VAL_USLOVIYA")
    private Long valUsloviya;

    @Column(name = "INS_DATE", nullable = false)
    private LocalDate insDate;

    @Column(name = "FIZYUR")
    private Integer fizyur;

    @Column(name = "INS_DOGNUM")
    private Long insDognum;

    @Column(name = "INS_OTV_SUM")
    private BigDecimal insOtvSum;

    @Column(name = "BENEFICIARY")
    private Long beneficiary;
}