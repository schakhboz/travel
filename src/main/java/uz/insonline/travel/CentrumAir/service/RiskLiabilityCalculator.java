package uz.insonline.travel.CentrumAir.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.insonline.travel.CentrumAir.domain.ProductSelection;
import uz.insonline.travel.CentrumAir.domain.RiskCatalog;
import uz.insonline.travel.CentrumAir.entity.InsCentrumAirRiskEntity;
import uz.insonline.travel.CentrumAir.repository.InsCentrumAirRiskRepository;

import java.math.BigDecimal;

/** Страховые суммы по рискам и учётным группам; суммы в EUR приводятся к UZS по курсу выпуска (ТЗ п. 3.1). */
@Service
@RequiredArgsConstructor
public class RiskLiabilityCalculator {

    private final InsCentrumAirRiskRepository riskRepository;

    public BigDecimal forRisk(String riskCode, BigDecimal exchangeRate) {
        BigDecimal total = BigDecimal.ZERO;
        for (InsCentrumAirRiskEntity risk : riskRepository.findAllByRiskCode(riskCode)) {
            BigDecimal sum = risk.getInsuranceSum();
            if ("EUR".equalsIgnoreCase(risk.getCurrency())) {
                sum = sum.multiply(exchangeRate);
            }
            total = total.add(sum);
        }
        return total;
    }

    public BigDecimal forGroup(int policyGroup, ProductSelection products, int passengerCount, BigDecimal exchangeRate) {
        BigDecimal total = BigDecimal.ZERO;
        for (String riskCode : RiskCatalog.risksForGroup(policyGroup, products)) {
            total = total.add(forRisk(riskCode, exchangeRate));
        }
        return total.multiply(BigDecimal.valueOf(passengerCount));
    }
}
