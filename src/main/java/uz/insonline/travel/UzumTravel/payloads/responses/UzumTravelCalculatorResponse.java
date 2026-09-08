package uz.insonline.travel.UzumTravel.payloads.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import uz.insonline.travel.commons.payload.response.ApiResponseAll;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Schema(name = "UzumTravelCalculatorResponse")
public class UzumTravelCalculatorResponse extends ApiResponseAll {


    @Schema(description = "Premium in UZS")
    private Double premium;


    @Schema(description = "Liability limit (insurance amount)")
    private Double coverageAmount;

    public UzumTravelCalculatorResponse(int result, String message, Double premium, Double liability) {
        super(result, message);
        this.premium = premium;
        this.coverageAmount = liability;
    }

}

//public class AirTravelCalculatorResponse {
//
//    private int result;
//    private String result_message;
//
//    // Оставляем только одно поле премии
//    private double premium;
//
//    // И поле ответственности
//    private double coverageAmount;
//}