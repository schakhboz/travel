package uz.insonline.travel.UzumTravel.payloads.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import uz.insonline.travel.commons.payload.request.ApiRequest;

@Data
@Schema(name = "UzumTravelPolicyRequest", description = "Request for policy information")
public class UzumTravelPolicyRequest extends ApiRequest {

    @Schema(description = "Contract ID", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Contract ID can not be null")
    private Long contract_id;

}
