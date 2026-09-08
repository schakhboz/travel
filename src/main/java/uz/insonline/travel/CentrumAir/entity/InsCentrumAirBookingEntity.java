package uz.insonline.travel.CentrumAir.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "INS_CENTRUM_AIR_BOOKINGS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InsCentrumAirBookingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ins_centrum_air_booking_seq_gen")
    @SequenceGenerator(name = "ins_centrum_air_booking_seq_gen",
            sequenceName = "INS_CENTRUM_AIR_BOOKING_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "PNR", nullable = false, length = 50)
    private String pnr;

    @Column(name = "PAYMENT_TIME", nullable = false)
    private OffsetDateTime paymentTime;

    @Column(name = "SALES_CHANNEL", length = 50)
    private String salesChannel;

    @Column(name = "ROUTE_TYPE", length = 10)
    private String routeType;

    @Column(name = "IS_INTERNATIONAL")
    private Boolean isInternational;

    @Column(name = "IS_SCHENGEN")
    private Boolean isSchengen;
}