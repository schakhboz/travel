package uz.insonline.travel.CentrumAir.dictionary.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/** Риск из реестра: класс страхования и страховая сумма (ТЗ п. 3.1). */
@Schema(description = "Insurance risk")
public record RiskDto(

        @Schema(description = "Risk id", example = "4")
        Long id,

        @Schema(description = "Risk code", example = "ACCIDENT")
        String riskCode,

        @Schema(description = "Risk title", example = "НС: смерть, инвалидность I–III гр., травма")
        String title,

        @Schema(description = "Insurance class according to the legislation of Uzbekistan", example = "1")
        String classId,

        @Schema(description = "Insured sum per person or item", example = "105000000")
        BigDecimal insuredSum,

        @Schema(description = "Insured sum currency", example = "UZS")
        String currency
) {
}
