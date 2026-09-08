package uz.insonline.travel.authentication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "INS_ANKETA")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Anketa {

    @Id
    @Column(name = "INS_ID")
    Long id;

    @Column(name = "INS_TYPE")
    Long insuranceType;

    @Column(name = "INS_NUM")
    String contractNumber;

    @Column(name = "INS_ID_PAR")
    Long idPar;

    @Column(name = "INS_STAT")
    Long insStat;
}
