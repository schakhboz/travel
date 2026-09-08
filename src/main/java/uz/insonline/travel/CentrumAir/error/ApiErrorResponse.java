package uz.insonline.travel.CentrumAir.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Machine-readable API error (ТЗ п. 7.6.6)")
public record ApiErrorResponse(

        @Schema(description = "Result code: -1 – error", example = "-1")
        int result,

        @Schema(description = "Human-readable description in English", example = "Field 'pnr' is required")
        @JsonProperty("result_message") String resultMessage,

        @Schema(description = "Machine-readable error code", example = "validation_error")
        String code,

        @Schema(description = "Request identifier for support and log correlation")
        @JsonProperty("request_id") String requestId
) {
    public static ApiErrorResponse of(CentrumAirErrorCode code, String message, String requestId) {
        return new ApiErrorResponse(-1, message, code.code(), requestId);
    }
}
