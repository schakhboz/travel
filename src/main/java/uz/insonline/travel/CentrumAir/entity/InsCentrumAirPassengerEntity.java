package uz.insonline.travel.CentrumAir.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "INS_CENTRUM_AIR_PASSENGERS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InsCentrumAirPassengerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ins_centrum_air_passenger_seq_gen")
    @SequenceGenerator(name = "ins_centrum_air_passenger_seq_gen",
            sequenceName = "INS_CENTRUM_AIR_PASSENGER_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "CONTRACT_ID", nullable = false)
    private Long contractId;

    @Column(name = "CLIENT_ID", nullable = false)
    private Long clientId;

    @Column(name = "BOOKING_ID", nullable = false)
    private Long bookingId;

    @Column(name = "FLIGHT_NUMBER", length = 20)
    private String flightNumber;

    @Column(name = "DEPARTURE_AIRPORT", length = 10)
    private String departureAirport;

    @Column(name = "ARRIVAL_AIRPORT", length = 10)
    private String arrivalAirport;

    @Column(name = "DEPARTURE_TIME")
    private OffsetDateTime departureTime;

    @Column(name = "ARRIVAL_TIME")
    private OffsetDateTime arrivalTime;

    @Column(name = "SEGMENT_ORDER")
    private Integer segmentOrder;

    @Column(name = "RISK_CODES", length = 500)
    private String riskCodes;
}
