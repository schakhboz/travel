package uz.insonline.travel.CentrumAir.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "INS_KONTRAGENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsKontragentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ins_kontragent_seq_gen")
    @SequenceGenerator(
            name = "ins_kontragent_seq_gen",
            sequenceName = "INS_KONTRAGENT_SEQ",
            allocationSize = 1
    )
    @Column(name = "TB_ID", nullable = false)
    private Long tbId;

    @Column(name = "TB_MASTERID")
    private Long tbMasterid;

    @Column(name = "TB_FIZYUR")
    private Integer tbFizyur;

    @Column(name = "TB_NAME", length = 128)
    private String tbName;

    @Column(name = "TB_SURNAME", length = 128)
    private String tbSurname;

    @Column(name = "TB_PATRONYM", length = 128)
    private String tbPatronym;

    @Column(name = "TB_PASPSERY", length = 20)
    private String tbPaspsery;

    @Column(name = "TB_PASPNUMBER", length = 40)
    private String tbPaspnumber;

    @Column(name = "TB_SEX")
    private Integer tbSex;

    @Column(name = "TB_DATEBIRTH")
    private LocalDate tbDatebirth;

    @Column(name = "TB_PHONE1", length = 40)
    private String tbPhone1;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "MOD_USER")
    private Long modUser;

    @Column(name = "TB_EMAIL", length = 200)
    private String tbEmail;

    @Column(name = "TB_ULICA", length = 1000)
    private String tbUlica;

    @Column(name = "TB_INPS")
    private Long tbInps;

    @Column(name = "TB_REZIDENT")
    private Integer tbRezident;

    @Column(name = "TB_COUNTRY")
    private Long tbCountry;

    @Column(name = "TB_OBLAST")
    private Long tbOblast;

    @Column(name = "TB_RAYON")
    private Long tbRayon;
}