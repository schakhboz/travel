package uz.insonline.travel.CentrumAir.dictionary.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/** Строка тарифной матрицы: ставка риска в разрезе продукта и типа маршрута (ТЗ п. 5.4). */
@Schema(description = "Tariff matrix entry")
public record TariffDto(

        @Schema(description = "Tariff id", example = "17")
        Long id,

        @Schema(description = "Policy group the tariff belongs to: 0 – travel, 1 – accident and baggage, "
                + "2 – trip cancellation, 3 – flight delay and documents", example = "1")
        Integer policyGroup,

        @Schema(description = "Product code the tariff is priced for: STANDARD, EXTENDED, MAXIMUM, TRAVEL, "
                + "ADDON_BAGGAGE, ANIMAL", example = "MAXIMUM")
        String tariffCode,

        @Schema(description = "Risk the tariff belongs to", example = "ACCIDENT")
        String riskCode,

        @Schema(description = "Risk title", example = "Несчастный случай во время перелёта")
        String riskTitle,

        @Schema(description = "Gross rate for a one-way route", example = "30000")
        BigDecimal oneWaySum,

        @Schema(description = "Gross rate for a round-trip route", example = "41600")
        BigDecimal roundTripSum,

        @Schema(description = "Rate currency: UZS for fixed rates, EUR for the daily travel rate", example = "UZS")
        String currency
) {
}
