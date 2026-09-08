package uz.insonline.travel.TravelV2.payloads.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uz.insonline.travel.commons.payload.response.ApiResponseAll;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(name="contract")
public class ContractResponse extends ApiResponseAll {

    private Long contract_id;
    private String commission;
    private String uuid;

    public ContractResponse(int result, String resultMessage) {
        super(result, resultMessage);
    }

    public ContractResponse(int result, String resultMessage, Long contract_id) {
        super(result, resultMessage);
        this.contract_id = contract_id;
    }

    public ContractResponse(int result, String resultMessage, Long contract_id, String commission, String uuid) {
        super(result, resultMessage);
        this.contract_id = contract_id;
        this.commission = commission;
        this.uuid = uuid;
    }
}