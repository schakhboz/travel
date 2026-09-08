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
public class PersonPinflRequestDto {

    @Schema(description = "pinfl",example ="30101995750028")
    @NotEmpty(message = "pinfl cannot be Empty")
    private String pinfl;

    @Schema(description = "passportSeries",example ="AB")
    @NotEmpty(message = "passport Series cannot be Empty")
    private String passportSeries;

    @Schema(description = "passport Number",example ="0160608")
    @NotEmpty(message = "passport Number cannot be ")
    private String passportNumber;
}
