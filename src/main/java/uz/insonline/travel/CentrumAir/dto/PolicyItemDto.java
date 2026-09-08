package uz.insonline.travel.CentrumAir.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Individual policy details")
public record PolicyItemDto(
        @Schema(description = "Policy group (0,1,2,3)", example = "0")
        Integer policyGroup,

        @Schema(description = "Contract ID in the insurance system", example = "672077")
        Long contractId,

        @Schema(description = "Internal policy ID", example = "867833")
        Long policyId,

        @Schema(description = "Policy series", example = "EIND")
        String policySeries,

        @Schema(description = "Policy number", example = "2834")
        String policyNumber,

        @Schema(description = "Policy UUID (external reference)", example = "2834")
        String policyUuid,

        @Schema(description = "Policy status (e.g., ISSUED, CANCELLED)", example = "ISSUED")
        String status,

        @Schema(description = "Premium amount for this policy", example = "388800")
        BigDecimal premiumAmount,

        @Schema(description = "Liability (insured sum) for this policy", example = "1215000000")
        BigDecimal liabilityAmount,

        @Schema(description = "List of risk codes covered by this policy", example = "[\"TRAVEL\"]")
        List<String> riskCodes,

        @Schema(description = "Start date of coverage (ISO 8601 date)", example = "2026-09-04")
        String startDate,

        @Schema(description = "End date of coverage (ISO 8601 date)", example = "2026-09-15")
        String endDate,

        @Schema(description = "URL to the PDF version of the policy (if available)", example = "null")
        String erspPdfUrl
) {
}
