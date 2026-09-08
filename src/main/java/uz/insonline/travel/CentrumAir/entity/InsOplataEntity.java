package uz.insonline.travel.CentrumAir.entity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "INS_OPLATA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsOplataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ins_oplata_seq_gen")
    @SequenceGenerator(
            name = "ins_oplata_seq_gen",
            sequenceName = "INS_OPLATA_SEQ",
            allocationSize = 1
    )
    @Column(name = "INS_ID", nullable = false)
    private Long insId;

    @Column(name = "ANKETA_ID")
    private Long anketaId;

    @Column(name = "POLIS_ID")
    private Long polisId;

    @Column(name = "OPL_SUMMA", precision = 18, scale = 2)
    private BigDecimal oplSumma;

    @Column(name = "OPLATA")
    private Long oplata;

    @Column(name = "STATUS")
    private Integer status;

    @Column(name = "INS_TYPE", nullable = false)
    private Long insType;

    @Column(name = "OPL_TYPE")
    private Integer oplType;

    @Column(name = "DIVISION_ID", nullable = false)
    private Long divisionId;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "OPL_DATA")
    private LocalDate oplData;

    @Column(name = "VAL_TYPE")
    private Long valType;

    @Column(name = "VAL_KURS", precision = 12, scale = 2)
    private BigDecimal valKurs;

    @Column(name = "OPL_VAL", precision = 18, scale = 2)
    private BigDecimal oplVal;

    @Column(name = "AGENCY_TRANSACTION_ID", length = 36)
    private String agencyTransactionId;
}