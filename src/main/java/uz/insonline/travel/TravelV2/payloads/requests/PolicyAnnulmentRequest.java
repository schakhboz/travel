package uz.insonline.travel.TravelV2.payloads.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uz.insonline.travel.authentication.payload.request.ApiRequest;

/**
 * @className: PolicyTerminateRequest
 * @date: 10.04.2025
 * @author: Uralbaev Diyorbek
 */

@Getter
@Setter
@ToString
@Schema(name="policy-annulment")
public class PolicyAnnulmentRequest extends ApiRequest {

    @Schema(description = "ID of insurance contract obtained from \"/api/travel/contract\" method", example = "123456")
    @NotNull(message = "Contract ID can not be null")
    private String contractId;

    @Schema(description = """
            Reason types: \n
            0  => other (reasonText must write), \n
            1  => test was conducted, \n
            2  => incorrect date, \n
            3  => insured incorrectly indicated, \n
            4  => insurant person incorrectly indicated, \n
            5  => the insured amount was incorrectly indicated, \n
            6  => insurance premium incorrectly indicated, \n
            7  => couldn't show separate notes, \n
            8  => incorrectly insurance product, \n
            10 => insured value was shown incorrectly, \n
            11 => insured items incorrectly
            """, defaultValue = "1")
    @NotNull(message = "reasonNumber can not be null")
    private Integer reasonNumber;

    @Schema(description = "If reasonNumber is 0, then reasonText must be enter")
    private String reasonText;
}
