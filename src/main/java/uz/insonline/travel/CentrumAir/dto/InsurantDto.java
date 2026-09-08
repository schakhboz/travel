package uz.insonline.travel.CentrumAir.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Schema(description = "Insurant personal information")
public record InsurantDto(

        @Schema(description = "Phone number with country code", example = "+998901111111")
        @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Invalid phone number format")
        String phone,

        @Schema(description = "Email address", example = "insurant@example.com")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "Residential address", example = "г. Ташкент, ул. Навои, 1")
        String address,

        @Schema(description = "Resident type: 1 – resident, 2 – non-resident", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Resident type is required")
        Integer residentType,

        @Schema(description = "PINFL (personal identification number)", example = "321512141251421")
        @Pattern(regexp = "\\d{14}", message = "PINFL must consist of 14 digits")
        String pinfl,

        @Schema(description = "Passport series", example = "AB", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Passport series is required")
        @Size(max = 10, message = "Passport series cannot exceed 10 characters")
        String passportSeries,

        @Schema(description = "Passport number", example = "1111111", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Passport number is required")
        @Size(max = 20, message = "Passport number cannot exceed 20 characters")
        String passportNumber,

        @Schema(description = "First name", example = "Ivanov", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "First name is required")
        @Size(max = 128, message = "First name cannot exceed 128 characters")
        String firstName,

        @Schema(description = "Last name", example = "Ivan", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Last name is required")
        @Size(max = 128, message = "Last name cannot exceed 128 characters")
        String lastName,

        @Schema(description = "Middle name", example = "Ivanovich")
        @Size(max = 128, message = "Middle name cannot exceed 128 characters")
        String middleName,

        @Schema(description = "Birth date in yyyy-MM-dd format", example = "1995-08-26")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate birthDate,

        @Schema(description = "Gender: 1 – male, 2 – female", example = "1")
        Integer gender,

        @Schema(description = "Citizenship ID", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Citizenship is required")
        Integer citizenshipId
) {
}
