package uz.insonline.travel.CentrumAir.entity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "INS_POLIS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsPolisEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tb_polis_seq_gen")
    @SequenceGenerator(
            name = "tb_polis_seq_gen",
            sequenceName = "Sq_Tb_Polis",
            allocationSize = 1
    )
    @Column(name = "TB_ID", nullable = false)
    private Long tbId;

    @Column(name = "TB_SERY", nullable = false, length = 20)
    private String tbSery;

    @Column(name = "TB_NUMBER", nullable = false)
    private Long tbNumber;

    @Column(name = "TB_ANKETA")
    private Long tbAnketa;

    @Column(name = "TB_STATUS", nullable = false)
    private Integer tbStatus;

    @Column(name = "TB_DATE_BEGIN")
    private LocalDate tbDateBegin;

    @Column(name = "TB_DATE_END")
    private LocalDate tbDateEnd;

    @Column(name = "TB_USER")
    private Long tbUser;

    @Column(name = "TB_SUMMA", precision = 18, scale = 2)
    private BigDecimal tbSumma;

    @Column(name = "TB_PREMIA", precision = 18, scale = 2)
    private BigDecimal tbPremia;

    @Column(name = "TB_DATEPRINT")
    private LocalDate tbDateprint;

    @Column(name = "TB_DATECONTROL")
    private LocalDate tbDatecontrol;

    @Column(name = "TB_DIVISION", nullable = false)
    private Long tbDivision;

    @Column(name = "VAL_TYPE")
    private Long valType;

    @Column(name = "VAL_KURS", precision = 12, scale = 2)
    private BigDecimal valKurs;

    @Column(name = "VAL_DATE")
    private LocalDate valDate;
}