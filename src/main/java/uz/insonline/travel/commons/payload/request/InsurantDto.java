package uz.insonline.travel.commons.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Schema(name = "Insurant", description = "Insurant data")
public class InsurantDto {

    @Schema(description = "Insurant type: 0 = Individual, 1 = Juridical", example = "1")
    @NotNull(message = "Insurant type is required")
    @Min(0) @Max(1)
    private Integer insurantType;

    @Schema(description = "Insurant phone (mandatory if resident)", example = "998901234567")
    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Invalid phone number format")
    private String phone;

    @Schema(description = "Insurant email (mandatory if non-resident)", example = "insurant@example.com")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "Address")
    private String address;

    @Schema(description = "Residency (1=Resident, 2=Non-resident)")
    @NotNull(message = "Insurant residency is required")
    private Integer residentType;


    @Schema(description = "Individual data (fill if insurantType = 0)")
    @Valid
    private InsurantPersonDto person;

    @Schema(description = "Organization data (fill if insurantType = 1)")
    @Valid
    private InsurantOrganizationDto organization;
}
