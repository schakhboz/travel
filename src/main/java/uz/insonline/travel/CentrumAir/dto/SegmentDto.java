package uz.insonline.travel.CentrumAir.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

@Schema(description = "Flight segment details")
public record SegmentDto(
        @Schema(description = "Order of the segment in the itinerary", example = "1")
        Integer segmentOrder,

        @Schema(description = "Flight number", example = "C6-101", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String flightNumber,

        @Schema(description = "IATA code of departure airport", example = "TAS", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String departureAirport,

        @Schema(description = "IATA code of arrival airport", example = "DXB", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String arrivalAirport,

        @Schema(description = "Scheduled departure time (UTC)", example = "2026-09-04T08:00:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull OffsetDateTime departureTime,

        @Schema(description = "Scheduled arrival time (UTC)", example = "2026-09-04T11:00:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull OffsetDateTime arrivalTime
) {}