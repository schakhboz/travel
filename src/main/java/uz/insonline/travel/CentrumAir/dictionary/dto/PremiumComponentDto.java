package uz.insonline.travel.CentrumAir.dictionary.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/** Слагаемое премии полиса: тарифная ставка, множитель и результат. */
@Schema(description = "Single term of the policy premium")
public record PremiumComponentDto(

        @Schema(description = "What is being priced: PACKAGE, TRAVEL, ADDON_BAGGAGE, ANIMAL", example = "PACKAGE")
        String component,

        @Schema(description = "Tariff code the rate is taken from", example = "MAXIMUM")
        String tariffCode,

        @Schema(description = "Rate from the tariff matrix for the requested route type", example = "41600")
        BigDecimal tariffAmount,

        @Schema(description = "Rate currency", example = "UZS")
        String tariffCurrency,

        @Schema(description = "Number of units the rate is multiplied by", example = "2")
        int units,

        @Schema(description = "What a unit is", example = "passengers")
        String unitName,

        @Schema(description = "Calculation as text", example = "41 600 × 2 passengers = 83 200")
        String formula,

        @Schema(description = "Component amount in UZS", example = "83200")
        BigDecimal amount
) {
}
