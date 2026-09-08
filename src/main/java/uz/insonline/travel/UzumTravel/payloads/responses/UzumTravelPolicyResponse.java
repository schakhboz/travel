package uz.insonline.travel.UzumTravel.payloads.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uz.insonline.travel.commons.payload.response.ApiResponseAll;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(name = "UzumTravelPolicyResponse")
public class UzumTravelPolicyResponse extends ApiResponseAll {

    @Schema(description = "Policy series")
    private String policySeries;

    @Schema(description = "Policy number")
    private String policyNumber;

    @Schema(description = "PDF download link")
    private String policyLink;

    public UzumTravelPolicyResponse(int result, String resultMessage, String policySeries, String policyNumber, String policyLink) {
        super(result, resultMessage);
        this.policySeries = policySeries;
        this.policyNumber = policyNumber;
        this.policyLink = policyLink;
    }

    public UzumTravelPolicyResponse(int result, String resultMessage) {
        super(result, resultMessage);
    }
}