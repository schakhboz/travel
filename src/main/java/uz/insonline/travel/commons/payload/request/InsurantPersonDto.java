package uz.insonline.travel.commons.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Schema(name = "Person", description = "Individual data")
public class InsurantPersonDto {
    @Schema(description = "PINFL (mandatory if resident)", example = "12345678901234")
    @Pattern(regexp = "\\d{14}", message = "PINFL must consist of 14 digits")
    private String pinfl;

    @Schema(description = "Passport series", example = "AA")
    @NotBlank(message = "Passport series is required")
    @Size(max = 10, message = "Passport series cannot exceed 10 characters")
    private String passportSeries;

    @Schema(description = "Passport number", example = "1234567")
    @NotBlank(message = "Passport number is required")
    @Size(max = 20, message = "Passport number cannot exceed 20 characters")
    private String passportNumber;

    @Schema(description = "First name (Latin)", example = "IVAN")
    @NotBlank(message = "First name is required")
    @Size(max = 128, message = "First name cannot exceed 128 characters")
    private String firstName;

    @Schema(description = "Last name (Latin)", example = "IVANOV")
    @NotBlank(message = "Last name is required")
    @Size(max = 128, message = "Last name cannot exceed 128 characters")
    private String lastName;

    @Schema(description = "Middle name (Latin)", example = "IVANOVICH")
    @Size(max = 128, message = "Middle name cannot exceed 128 characters")
    private String middleName;

    @Schema(description = "Birth date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @Schema(description = "Gender (1=M, 0=F)")
    private Integer gender;

    @Schema(description = "Citizenship ID (see api/travel/countries)")
    @NotNull(message = "Citizenship is required")
    @JsonProperty("citizenship_id")
    @JsonAlias({"citizenship", "citizenship_id", "citizenshipId"})
    private Integer citizenshipId;
}
