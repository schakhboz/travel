package uz.insonline.travel.CentrumAir.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "INS_CENTRUM_AIR_BOOKINGS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsCentrumAirBookingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_booking_gen")
    @SequenceGenerator(
            name = "seq_booking_gen",
            sequenceName = "SEQ_INS_CENTRUM_AIR_BOOKINGS",
            allocationSize = 1
    )
    @Column(name = "ID")
    private Long id;

    @Column(name = "PNR", nullable = false, length = 20)
    private String pnr;

    @Column(name = "PAYMENT_TIME", nullable = false)
    private OffsetDateTime paymentTime;

    @Column(name = "SALES_CHANNEL", nullable = false, length = 30)
    private String salesChannel;

    @Column(name = "ROUTE_TYPE", nullable = false, length = 10)
    private String routeType;

    @Column(name = "IS_INTERNATIONAL", nullable = false)
    private Integer isInternational;

    @Column(name = "IS_SCHENGEN", nullable = false)
    private Integer isSchengen;

    @Column(name = "CREATED_AT", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
