package uz.insonline.travel.CentrumAir.dto.policyCancel;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to cancel an insurance policy")
public record CancelPolicyRequest(

        @Schema(description = "Contract ID of the policy to cancel", example = "669301", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Contract ID is required")
        Long contractId,

        @Schema(description = "Reason number from reference table (0 = custom text, others from dictionary)", example = "1")
        Integer reasonNumber,

        @Schema(description = "Custom reason text (used when reasonNumber = 0)", example = "test was conducted")
        String reasonText
) {
}
