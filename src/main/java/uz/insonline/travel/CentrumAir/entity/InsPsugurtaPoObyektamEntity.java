package uz.insonline.travel.CentrumAir.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "INS_PSUGURTA_PO_OBYEKTAM")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsPsugurtaPoObyektamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ins_sugurta_po_obyektam_seq_gen")
    @SequenceGenerator(
            name = "ins_sugurta_po_obyektam_seq_gen",
            sequenceName = "INS_PSUGURTA_PO_OBYEKTAM_SEQ",
            allocationSize = 1
    )
    @Column(name = "INS_ID", nullable = false)
    private Long insId;

    @Column(name = "ANKETA_ID")
    private Long anketaId;

    @Column(name = "AVTO_ID")
    private Long avtoId;

    @Column(name = "LINK_ID")
    private Long linkId;

    @Column(name = "PTURI_ID")
    private Long pturiId;

    @Column(name = "DIVISION_ID")
    private Long divisionId;

    @Column(name = "VAL_TYPE")
    private Long valType;

    @Column(name = "VAL_DATE")
    private LocalDate valDate;

    @Column(name = "VAL_KURS", precision = 12, scale = 2)
    private BigDecimal valKurs;

    @Column(name = "MUKOFOT_F", precision = 12, scale = 2)
    private BigDecimal mukofotF;

    @Column(name = "MUKOFOT", precision = 18, scale = 2)
    private BigDecimal mukofot;

    @Column(name = "MAJBURIYAT", precision = 18, scale = 2)
    private BigDecimal majburiyat;

    @Column(name = "OBEKT_SONI")
    private Integer obektSoni;

    @Column(name = "MUKOFOT2", precision = 18, scale = 2)
    private BigDecimal mukofot2;
}