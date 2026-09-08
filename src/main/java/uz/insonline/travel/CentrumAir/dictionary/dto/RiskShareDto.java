package uz.insonline.travel.CentrumAir.dictionary.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/** Доля риска в премии полиса и его страховая сумма (ТЗ п. 5.4). */
@Schema(description = "Risk share of the policy premium")
public record RiskShareDto(

        @Schema(description = "Risk code", example = "ACCIDENT")
        String riskCode,

        @Schema(description = "Risk title", example = "НС: смерть, инвалидность I–III гр., травма")
        String title,

        @Schema(description = "Insurance class", example = "1")
        String classId,

        @Schema(description = "Insured sum per person or item", example = "105000000")
        BigDecimal insuredSum,

        @Schema(description = "Insured sum currency", example = "UZS")
        String insuredSumCurrency,

        @Schema(description = "Risk premium inside the policy, UZS", example = "41600")
        BigDecimal premiumAmount,

        @Schema(description = "Share of the policy premium, percent", example = "16.83")
        BigDecimal premiumShare
) {
}
