package uz.insonline.travel.authentication.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiPolicyRequest extends ApiRequest {
    @Schema(description = "contract_id", example = "1075232")
    @NotNull(message = "contract_id cannot be null")
    private Long contract_id;
}
