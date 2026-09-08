package uz.insonline.travel.CentrumAir.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Builder
@Schema(description = "Booking (PNR) with its associated policies")
public record IssueContentDto(
        @Schema(description = "PNR code", example = "PNR123456")
        String pnr,

        @Schema(description = "Issue date of the policy(ies)", example = "2026-09-04")
        LocalDate issueDate,

        @Schema(description = "Full name of the insurant", example = "ISMOILOV SHAXBOZ")
        String insurantName,

        @Schema(description = "Total premium amount for all policies in this booking", example = "500000")
        BigDecimal totalPremiumAmount,

        @Schema(description = "Currency code", example = "UZS")
        String premiumCurrency,

        @Schema(description = "List of policies issued for this booking")
        List<PolicyItemDto> policies
) {
}
