package uz.insonline.travel.CentrumAir.dto.policyCancel;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response after policy cancellation")
public record CancelPolicyResponse(
        @Schema(description = "Result code: 0 – success, -1 – error", example = "0")
        Integer result,

        @Schema(description = "Result message", example = "Successfully done!")
        String resultMessage
) {

    public static CancelPolicyResponse success() {
        return new CancelPolicyResponse(0, "Successfully done!");
    }
    public static CancelPolicyResponse error(String message) {
        return new CancelPolicyResponse(-1, message);
    }
}
