package uz.insonline.travel.CentrumAir.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Transaction reference for a specific policy group")
public record TransactionDto(
        @Schema(description = "Policy group number (0,1,2,3)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Integer policyGroup,

        @Schema(description = "External transaction ID", example = "bfd9df4b-4fbf-4513-8dca-e928fb28ed00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String transactionId
) {
}
