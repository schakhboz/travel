package uz.insonline.travel.Inbound.payload.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CalculatorResponse extends InboundApiResponse {
    private double premiumUzS; // Премия в сумах
    private double liability;  // Ответственность

    public CalculatorResponse(int result, String message) {
        super(result, message);
    }

    public CalculatorResponse(int result, String message, double premiumUzS, double liability) {
        super(result, message);
        this.premiumUzS = premiumUzS;
        this.liability = liability;
    }
}