package uz.insonline.travel.commons.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Schema(name = "Organization", description = "Organization data")
public class InsurantOrganizationDto {

    @Schema(description = "INN (Tax ID)")
    @Pattern(regexp = "\\d{9}", message = "INN must consist of 9 digits")
    private String inn;

}