package uz.insonline.travel.UzumTravel.payloads.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uz.insonline.travel.commons.payload.request.ApiRequest;

@Getter
@Setter
@ToString
@Schema(name = "UzumTravelActivateRequest", description = "Policy activation request")
public class UzumTravelActivateRequest extends ApiRequest {

    @Schema(description = "Contract ID obtained during creation (digits only)", example = "12345")
    @NotNull(message = "Contract ID is required")
    @Min(value = 1, message = "Contract ID must be a positive number")
    private Long contractId;

}