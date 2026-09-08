package uz.insonline.travel.authentication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "INS_POLIS")
public class Policy {
    @Id
    @Column(name = "TB_ID")
    private Long tbId;

    @Column(name = "TB_NUMBER")
    private Long policyNumber;

    @Column(name = "TB_SERY")
    private String policySery;

    @Column(name = "TB_ANKETA")
    private Long anketaId;

    @Column(name = "TB_USER")
    private Long userId;

    @Column(name = "FOND_UID")
    private String fondUid;

    @Column(name = "TB_STATUS")
    private Long status;
}
