package uz.insonline.travel.log.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "LOG_API_SERVER")
public class LogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "LOG_API_SERVER_SEQ")
    @SequenceGenerator(name = "LOG_API_SERVER_SEQ", sequenceName = "LOG_API_SERVER_SEQ", allocationSize = 1)
    @Column(name = "LOG_ID")
    private int id;

    @Column(name = "LOG_DATE")
    private Date logDate;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "REQ_CONTENT", columnDefinition = "CLOB")
    private String reqContent;

    @Column(name = "RES_CODE")
    private Integer resCode;

    @Column(name = "RES_MESSAGE")
    private String resMessage;

    @Column(name = "RES_CONTENT", columnDefinition = "CLOB")
    private String resContent;

    @Column(name = "TIMESPENT")
    private Long timeSpent;

    @Column(name = "TB_ANKETA")
    private Long anketaId;

    @Column(name = "PRODUCT_TYPE")
    private Integer productTypeId;

    @Column(name = "METHOD")
    private String method;

    @Column(name = "MICROSERVICE")
    private String microservice;
}
