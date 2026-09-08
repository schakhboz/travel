package uz.insonline.travel.Provider.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Valid
public class VehicleRequestDto {


    @Schema(description = "серия тех. пасп.",example ="AAF")
    @NotEmpty(message = "techPassportSeria cannot be Empty")
    private String techPassportSeria;

    @Schema(description = "Номер тех. пасп. (Длина номера техпаспорта авто, зарегистрированного в Узбекистане, должна составлять 7 символов.)",example ="3523002")
    @NotEmpty(message = "techPassportNumber cannot be Empty")
    private String techPassportNumber;

    @Schema(description = "государственный номер транспорт",example ="10X654WA")
    @NotEmpty(message = "govNumber cannot be Empty")
    private String govNumber;
}
