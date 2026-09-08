package uz.insonline.travel.TravelV2.payloads.responses;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uz.insonline.travel.commons.payload.response.ApiResponseAll;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Schema(name="calculator")
public class CalculatorResponse extends ApiResponseAll {
    @Schema(description = "Calculator result (premium, liability) in UZS")
    private CalculatorCurrency UZS;
    @Schema(description = "Calculator result (premium, liability) in USD")
    private CalculatorCurrency USD;
    @Schema(description = "Kurs (exchange rate) UZS -> USD", example = "12700")
    private Double kurs;

    public CalculatorResponse(int result, String result_message, double insurancePremiumUZS, double insurancePremiumUSD, double insuranceLiabilityUZS, double insuranceLiabilityUSD, double kurs) {
        super(result, result_message);
        this.UZS = new CalculatorCurrency(insurancePremiumUZS, insuranceLiabilityUZS);
        this.USD = new CalculatorCurrency(insurancePremiumUSD, insuranceLiabilityUSD);
        this.kurs = kurs;
    }

    public CalculatorResponse(int result, String result_message) {
        super(result, result_message);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class CalculatorCurrency {
        @Schema(description = "Insurance cost (premia)")
        private Double premium;
        @Schema(description = "Insurance liability (otv)")
        private Double liability;
    }
}
