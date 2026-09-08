package uz.insonline.travel.UzumTravel.payloads.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import uz.insonline.travel.commons.payload.request.ApiRequest;
import uz.insonline.travel.commons.payload.request.InsurantDto;
import uz.insonline.travel.commons.payload.request.TravelerDto;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Schema(name = "UzumTravelContractRequest", description = "Contract creation request")
public class UzumTravelContractRequest extends ApiRequest {

    // === TRIP DETAILS ===
    @Schema(description = "Insurance start date (departure)")
    @NotNull(message = "Start date is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "Insurance end date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @NotNull(message = "End date is required")
    private LocalDate endDate;

    // === INSURANT ===
    @Schema(description = "Insurant data")
    @NotNull(message = "Insurant block is required")
    @Valid
    private InsurantDto insurant;

    @Schema(description = "List of travelers")
    @Valid
    @NotNull
    @Size(min = 1, message = "There must be at least one traveler")
    private List<TravelerDto> travelers;
}