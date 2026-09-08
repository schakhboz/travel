package uz.insonline.travel.CentrumAir.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErspResponse {

    private String policyType;
    private Long policyId;
    private String policySeries;
    private Long policyNumber;
    private String policyUuid;
    private BigDecimal premiumAmount;
    private BigDecimal liabilityAmount;
    private String riskCodes;
    private Integer objectCount;
    private Long contractId;
    private Integer policyGroup;
    private String startDate;
    private String endDate;
}
