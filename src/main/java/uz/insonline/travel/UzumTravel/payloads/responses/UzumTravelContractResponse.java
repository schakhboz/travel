package uz.insonline.travel.UzumTravel.payloads.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uz.insonline.travel.commons.payload.response.ApiResponseAll;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(name = "UzumTravelContractResponse")
public class UzumTravelContractResponse extends ApiResponseAll {

    @Schema(description = "Created contract ID (draft)")
    private Long contractId;

    public UzumTravelContractResponse(int result, String resultMessage, Long contractId) {
        super(result, resultMessage);
        this.contractId = contractId;
    }

    public UzumTravelContractResponse(int result, String resultMessage) {
        super(result, resultMessage);
    }
}