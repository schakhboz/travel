package uz.insonline.travel.CentrumAir.dto.calculation;

import java.math.BigDecimal;

public record PolicyGroupCalculation(
        int policyGroup,
        BigDecimal premiumAmount
) {
}
