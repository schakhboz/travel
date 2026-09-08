package uz.insonline.travel.Travel.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uz.insonline.travel.commons.payload.response.ApiResponseAll;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(name="ContractResponse-v1")
public class ContractResponse extends ApiResponseAll {
    @Schema(description = "Contract id", example = "107532")
    private Long contract_id;

    public ContractResponse(int result, String result_message, long contract_id) {
        super(result, result_message);
        this.contract_id = contract_id;
    }

    public ContractResponse(int result, String result_message) {
        super(result, result_message);
    }
}
