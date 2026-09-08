package uz.insonline.travel.CentrumAir.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "INS_CENTRUM_AIR_PASSENGERS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsCentrumAirPassengersEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_passengers_gen")
    @SequenceGenerator(
            name = "seq_passengers_gen",
            sequenceName = "SEQ_INS_CENTRUM_AIR_PASSENGERS",
            allocationSize = 1
    )
    @Column(name = "ID")
    private Long id;

    @Column(name = "CONTRACT_ID", nullable = false)
    private Long contractId;

    @Column(name = "CLIENT_ID", nullable = false)
    private Long clientId;

    @Column(name = "BOOKING_ID")
    private Long bookingId;

    @Column(name = "FLIGHT_NUMBER", nullable = false, length = 20)
    private String flightNumber;

    @Column(name = "DEPARTURE_AIRPORT", nullable = false, length = 10)
    private String departureAirport;

    @Column(name = "ARRIVAL_AIRPORT", nullable = false, length = 10)
    private String arrivalAirport;

    @Column(name = "DEPARTURE_TIME", nullable = false)
    private OffsetDateTime departureTime;

    @Column(name = "ARRIVAL_TIME", nullable = false)
    private OffsetDateTime arrivalTime;

    @Column(name = "SEGMENT_ORDER", nullable = false)
    private Integer segmentOrder;

    @Column(name = "RISK_CODES", length = 255)
    private String riskCodes;

    @Column(name = "CREATED_AT", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
