package uz.insonline.travel.CentrumAir.dictionary.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

/** Ответ калькулятора: состав полисов и премия с полной расшифровкой расчёта. */
@Schema(description = "Premium calculation result")
public record CalculationResponse(

        @Schema(description = "Result code: 0 – success", example = "0")
        int result,

        @Schema(description = "Result message", example = "Success")
        String resultMessage,

        @Schema(description = "Premium currency", example = "UZS")
        String currency,

        @Schema(description = "EUR rate applied to EUR-denominated tariffs", example = "13500")
        BigDecimal eurRate,

        @Schema(description = "Insured days used for the travel daily rate", example = "11")
        Long insuredDays,

        @Schema(description = "Total premium of the booking, UZS", example = "619600")
        BigDecimal totalPremiumAmount,

        @Schema(description = "Policies the booking is split into")
        List<PolicyPremiumDto> policies
) {
    public static CalculationResponse success(BigDecimal eurRate, Long insuredDays,
                                              BigDecimal total, List<PolicyPremiumDto> policies) {
        return new CalculationResponse(0, "Success", "UZS", eurRate, insuredDays, total, policies);
    }
}
