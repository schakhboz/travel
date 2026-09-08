package uz.insonline.travel.TravelV2.payloads.requests;

import lombok.*;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import uz.insonline.travel.authentication.payload.request.ApiRequest;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for policy information")
//@EqualsAndHashCode(callSuper = true)
public class PolicyRequestV2 {

    @Schema(description = "ID of insurance contract obtained from \"contract\\create\" method", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Contract ID can not be null")
    private Integer contract_id;

}
