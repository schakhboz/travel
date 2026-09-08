package uz.insonline.travel.CentrumAir.service;

import uz.insonline.travel.CentrumAir.domain.ProductSelection;
import uz.insonline.travel.CentrumAir.dto.calculation.PolicyGroupCalculation;
import uz.insonline.travel.CentrumAir.dto.request.PolicyIssueRequest;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Всё, что нужно для выпуска одного полиса учётной группы. */
public record GroupIssueCommand(
        PolicyIssueRequest request,
        ProductSelection products,
        PolicyGroupCalculation calculation,
        Long userId,
        Long divisionId,
        Long bookingId,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal exchangeRate
) {
    public int policyGroup() {
        return calculation.policyGroup();
    }

    public BigDecimal premium() {
        return calculation.premiumAmount();
    }
}
