package uz.insonline.travel.commons.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Schema(name = "Traveler", description = "Traveler data")
public class TravelerDto {
    @Schema(description = "Passenger flight number", example = "HY-301")
    @NotBlank(message = "Flight number is required for each passenger")
    private String flightNumber;

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

    @Schema(description = "Middle name (if any)", example = "IVANOVICH")
    @Size(max = 128, message = "Middle name cannot exceed 128 characters")
    private String middleName;

    @Schema(description = "Gender: 1 - Male, 0 - Female", example = "1")
    @NotNull(message = "Gender is required")
    private Integer gender;

    @Schema(description = "Birth date", example = "1990-01-01")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @NotNull(message = "Birth date is required")
    private LocalDate birthDate;

    @Schema(description = "Citizenship ID (reference book)", example = "260")
    @NotNull(message = "Citizenship is required")
    @JsonProperty("citizenship_id")
    @JsonAlias({"citizenship", "citizenship_id", "citizenshipId"})
    private Integer citizenshipId;

    @Schema(description = "Resident type: 1=Resident, 2=Non-resident", example = "1")
    @NotNull(message = "Resident type is required")
    private Integer residentType;

    @Schema(description = "PINFL (mandatory only for residents)", example = "12345678901234")
    @Pattern(regexp = "\\d{14}", message = "PINFL must consist of 14 digits")
    private String pinfl;

    @Schema(description = "Residence address", example = "Tashkent city...")
    private String address;

    @Schema(description = "List of risk IDs")
    @NotNull(message = "Risk list is required")
    @Size(min = 1, message = "Select at least one risk")
    private List<@Min(value = 1, message = "Min risk ID is 1") Integer> riskIds;

    @Schema(description = "Trip type: 1=OneWay, 2=RoundTrip", example = "1")
    @NotNull(message = "Trip type is required")
    private Integer tripType;

    @Schema(description = "Passenger email (optional)")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "Passenger phone number (optional)", example = "998901234567")
    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Invalid phone number format")
    private String phone;

}
