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
public class OrganizationRequestDto {
    @Schema(description = "Organization inn",example ="309341882")
    @NotEmpty(message = "inn cannot be Empty")
    private String inn;
}
