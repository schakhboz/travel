package uz.insonline.travel.Travel.payload.response;

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
public class GetPolicyResponse {

    @Schema(description = "Код результата обработки запроса", example = "0")
    private Integer result;

    @Schema(description = "Текстовое описание результата", example = "Успешная обработка запроса.")
    @JsonProperty("result_message")
    private String resultMessage;

    @Schema(description = "ID контракта", example = "649651")
    @JsonProperty("contract_id")
    private Long contractId;

    @Schema(description = "Номер договора", example = "XXXX/XXXX/X/XXXXXXXX")
    @JsonProperty("contract_num")
    private String contractNum;

    @Schema(description = "Серия и номер полиса", example = "EAPL 1234567")
    @JsonProperty("policy_no")
    private String policyNo;

    @Schema(description = "Текущий статус страхового полиса", example = "ISSUED")
    @JsonProperty("policy_status")
    private String policyStatus;

    @Schema(description = "Ссылки на полис на разных языках")
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
