package uz.insonline.travel.Travel.payload.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uz.insonline.travel.authentication.payload.request.ApiRequest;

@Getter
@Setter
@ToString
@Schema(name="PolicyCreateRequest")
public class PolicyCreateRequest extends ApiRequest {
    @Schema(description = "ID of insurance contract obtained from \"contract\\create\" method", example = "123456")
    @NotNull(message = "Contract ID can not be null")
    private Integer contractID;
}