package uz.insonline.travel.UzumTravel.payloads.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import uz.insonline.travel.commons.payload.request.ApiRequest;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Schema(name = "UzumTravelCalculatorRequest", description = "Policy cost calculation request")
public class UzumTravelCalculatorRequest extends ApiRequest {

    @Schema(description = "List of travelers")
    @JsonProperty("travelers")
    @Valid
    @NotNull(message = "Traveler list cannot be null")
    @Size(min = 1, message = "There must be at least one traveler")
    private List<CalcTravelerDto> travelers;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString

    @Schema(name = "CalculatorTraveler", description = "Calculation data for a single traveler")
    public static class CalcTravelerDto {

        @Schema(description = "List of risk IDs", example = "[1, 2]")
        @NotEmpty(message = "Risk list cannot be empty")
        private List<
                @NotNull(message = "Risk ID cannot be null")
                @Min(value = 1, message = "Risk ID must be at least 1")
                        Integer
                > riskIds;

        @Schema(description = "Trip type: 1=OneWay, 2=RoundTrip")
        @NotNull(message = "Trip type is required")
        @Min(value = 1, message = "Trip type must be 1 (OneWay) or 2 (RoundTrip)")
        @Max(value = 2, message = "Trip type must be 1 (OneWay) or 2 (RoundTrip)")
        private Integer tripType;

    }
}