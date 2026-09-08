package uz.insonline.travel.TravelV2.payloads.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import uz.insonline.travel.commons.payload.response.ApiResponseAll;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Schema(name="PolicyResponse")
public class PolicyResponse extends ApiResponseAll {
    @JsonIgnore
    @Value("${fond.policy-url}")
    private String url;
    private Policy policy = new Policy();
    private Contract contract = new Contract();

    public PolicyResponse(long contractID,
                          String contractUUID,
                          String policySeries,
                          String policyNumber,
                          long policyID,
                          String policyUrl,
                          int error,
                          String errorMessage
    ) {
        super(error, errorMessage);
        this.contract.id = contractID;
        this.contract.uuid = contractUUID;
        this.policy.id = policyID;
        this.policy.series = policySeries;
        this.policy.number = policyNumber;
        this.policy.url = policyUrl;
    }

    public PolicyResponse(int error, String errorMessage) {
        super(error, errorMessage);
    }

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Contract {
        private Long id;
        private String uuid;
    }

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Policy {
        private String series;
        private String number;
        private Long id;
        private String url;
    }
}
