package uz.insonline.travel.UzumTravel.payloads.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "UzumTravelGetPolicyResponse")
public class UzumTravelGetPolicyResponse {

    @Schema(description = "Result code", example = "0")
    private Integer result;

    @Schema(description = "Result message", example = "Request processed successfully.")
    @JsonProperty("result_message")
    private String resultMessage;

    @Schema(description = "Contract ID", example = "649651")
    @JsonProperty("contract_id")
    private Long contractId;

    @Schema(description = "Contract number", example = "XXXX/XXXX/X/XXXXXXXX")
    @JsonProperty("contract_num")
    private String contractNum;

    @Schema(description = "Policy series and number", example = "EAPL 1234567")
    @JsonProperty("policy_no")
    private String policyNo;

    @Schema(description = "Current policy status", example = "ISSUED")
    @JsonProperty("policy_status")
    private String policyStatus;

    @Schema(description = "Policy download links")
    @JsonProperty("policy_link")
    private PolicyLink policyLink;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PolicyLink {
        private String ru;
        private String uz;
        private String en;
    }

}
