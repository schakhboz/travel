package uz.insonline.travel.CentrumAir.dictionary.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

/** Полис одной учётной группы: из чего сложилась премия и как она распределена по рискам. */
@Schema(description = "Policy of one accounting group")
public record PolicyPremiumDto(

        @Schema(description = "Accounting group: 0 – travel, 1 – accident and baggage, 2 – trip cancellation, "
                + "3 – flight delay and documents", example = "1")
        int policyGroup,

        @Schema(description = "Human readable policy name", example = "П1 — НС, багаж, доп. багаж, питомец")
        String policyName,

        @Schema(description = "Policy premium, UZS", example = "247100")
        BigDecimal premiumAmount,

        @Schema(description = "Terms the premium is composed of")
        List<PremiumComponentDto> components,

        @Schema(description = "Risks covered by the policy with their premium shares")
        List<RiskShareDto> risks
) {
}
