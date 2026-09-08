package uz.insonline.travel.Travel.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uz.insonline.travel.authentication.payload.response.ApiResponseAll;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Schema(name="PolicyCreateResponse")
public class PolicyCreateResponse extends ApiResponseAll {

    @Schema(description = "Policy data. Information about insurance policy.")
    private Policy policy;

    @Schema(description = "Policy link. From where it is possible to download policy.")
    private String link;

    @Schema(description = "New Policy link. From where it is possible to download policy.")
    private String newPolicyLink;

    public PolicyCreateResponse(int error, String message, Policy policy, String link) {
        super(error, message);
        this.policy = policy;
        this.link = link;
    }

    public PolicyCreateResponse(int error, String message, Policy policy, String link, String newPolicyLink) {
        super(error, message);
        this.policy = policy;
        this.link = link;
        this.newPolicyLink = newPolicyLink;
    }

    public PolicyCreateResponse(int error, String message) {
        super(error, message);
    }

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    @Schema(name="Policy-v1")
    public static class Policy {
        @Schema(description = "Policy series.", example = "string")
        private String series;

        @Schema(description = "Policy number.", example = "0000123")
        private String number;
    }
}
