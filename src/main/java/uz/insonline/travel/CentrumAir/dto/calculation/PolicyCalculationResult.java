package uz.insonline.travel.CentrumAir.dto.calculation;


import java.math.BigDecimal;
import java.util.List;

public record PolicyCalculationResult(
        BigDecimal totalPremiumAmount,
        List<PolicyGroupCalculation> policyGroups
) {
}
