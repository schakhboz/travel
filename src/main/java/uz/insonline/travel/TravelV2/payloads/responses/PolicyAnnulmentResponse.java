package uz.insonline.travel.TravelV2.payloads.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import uz.insonline.travel.commons.payload.response.ApiResponseAll;

/**
 * @className: PolicyTerminateResponse
 * @date: 10.04.2025
 * @author: Uralbaev Diyorbek
 */

@Getter
@Schema(name = "policy-annulment")
public class PolicyAnnulmentResponse extends ApiResponseAll {

    public PolicyAnnulmentResponse(int result, String result_message) {
        super(result, result_message);
    }
}
