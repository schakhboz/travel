package uz.insonline.travel.Provider.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Valid
@Schema(name="PersonBirthDateRequestDto")
public class PersonBirthDateRequestDto {
    @Schema(description = "Birth Date. Example: 01.11.1999", example = "01.01.1999")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate birthDate;

    @Schema(description = "passport Series", example = "AB")
    @NotEmpty(message = "passport Series cannot be Empty")
    private String passportSeries;

    @Schema(description = "passport Number", example = "0160608")
    @NotEmpty(message = "passport Number cannot be ")
    private String passportNumber;
}
