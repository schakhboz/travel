package uz.insonline.travel.CentrumAir.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.insonline.travel.CentrumAir.domain.ProductSelection;
import uz.insonline.travel.CentrumAir.domain.RiskCatalog;
import uz.insonline.travel.CentrumAir.dto.SegmentDto;
import uz.insonline.travel.CentrumAir.dto.calculation.PolicyCalculationResult;
import uz.insonline.travel.CentrumAir.dto.calculation.PolicyGroupCalculation;
import uz.insonline.travel.CentrumAir.dto.request.PolicyIssueRequest;
import uz.insonline.travel.CentrumAir.entity.InsCentrumAirTariffEntity;
import uz.insonline.travel.CentrumAir.repository.InsCentrumAirTariffRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Состав полисов брони и премия по каждой учётной группе (ТЗ п. 4–5).
 * Риски разных учётных групп в один полис не объединяются, поэтому расчёт идёт группа за группой.
 */
@Service
@RequiredArgsConstructor
public class PolicyCalculationService {

    private final InsCentrumAirTariffRepository tariffRepository;

    public PolicyCalculationResult calculatePolicies(PolicyIssueRequest request, BigDecimal eurRate) {
        ProductSelection products = ProductSelection.of(request.products());
        List<SegmentDto> segments = request.route().segments();
        boolean isRT = isRoundTrip(request);
        int passengerCount = request.passengers().size();
        String packageCode = products.packageCode();

        List<PolicyGroupCalculation> groups = new ArrayList<>();

        // П0: ВЗР — тариф суточный, срок считается по маршруту (для Шенгена +15 дней к последнему прилёту).
        if (products.has(ProductSelection.TRAVEL)) {
            LocalDate startDate = segments.get(0).departureTime().toLocalDate();
            LocalDate endDate = Boolean.TRUE.equals(request.route().isSchengen())
                    ? segments.get(segments.size() - 1).arrivalTime().toLocalDate().plusDays(15)
                    : segments.get(segments.size() - 1).departureTime().toLocalDate();
            long insuredDays = ChronoUnit.DAYS.between(startDate, endDate);

            BigDecimal premium = tariffSum(0, List.of(ProductSelection.TRAVEL), isRT)
                    .multiply(BigDecimal.valueOf(insuredDays))
                    .multiply(eurRate)
                    .multiply(BigDecimal.valueOf(passengerCount));
            groups.add(new PolicyGroupCalculation(0, premium));
        }

        // П1: НС + багаж авиа-пакета + доп. багаж (за место) + питомец (за особь).
        BigDecimal groupOnePremium = BigDecimal.ZERO;
        if (packageCode != null) {
            groupOnePremium = groupOnePremium.add(
                    tariffSum(1, List.of(packageCode), isRT).multiply(BigDecimal.valueOf(passengerCount)));
        }
        groupOnePremium = groupOnePremium
                .add(perItemPremium(1, ProductSelection.ADDON_BAGGAGE, products, isRT))
                .add(perItemPremium(1, ProductSelection.ANIMAL, products, isRT));
        addIfPositive(groups, 1, groupOnePremium);

        // П2 (отмена поездки) и П3 (задержка рейса, документы) — только вместе с авиа-пакетом.
        if (packageCode != null) {
            addIfPositive(groups, 2, tariffSum(2, List.of(packageCode), isRT)
                    .multiply(BigDecimal.valueOf(passengerCount)));
            addIfPositive(groups, 3, tariffSum(3, List.of(packageCode), isRT)
                    .multiply(BigDecimal.valueOf(passengerCount)));
        }

        BigDecimal total = groups.stream()
                .map(PolicyGroupCalculation::premiumAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PolicyCalculationResult(total, groups);
    }

    /**
     * Разносит премию полиса по его рискам пропорционально тарифным долям (ТЗ п. 5.4).
     * Порядок ключей совпадает с порядком рисков в группе.
     */
    public Map<String, BigDecimal> splitPremiumByRisk(BigDecimal groupPremium, List<String> riskCodes,
                                                      String packageCode, boolean isRT) {
        Map<String, BigDecimal> tariffs = new LinkedHashMap<>();
        BigDecimal totalTariff = BigDecimal.ZERO;
        for (String riskCode : riskCodes) {
            BigDecimal tariff = riskTariff(riskCode, packageCode, isRT);
            tariffs.put(riskCode, tariff);
            totalTariff = totalTariff.add(tariff);
        }

        Map<String, BigDecimal> premiums = new LinkedHashMap<>();
        for (Map.Entry<String, BigDecimal> tariff : tariffs.entrySet()) {
            premiums.put(tariff.getKey(),
                    groupPremium.multiply(tariff.getValue()).divide(totalTariff, 2, RoundingMode.HALF_UP));
        }
        return premiums;
    }

    public static boolean isRoundTrip(PolicyIssueRequest request) {
        return "RT".equalsIgnoreCase(request.route().routeType());
    }

    private BigDecimal perItemPremium(int policyGroup, String productCode, ProductSelection products, boolean isRT) {
        int quantity = products.quantity(productCode);
        return quantity > 0
                ? tariffSum(policyGroup, List.of(productCode), isRT).multiply(BigDecimal.valueOf(quantity))
                : BigDecimal.ZERO;
    }

    private BigDecimal riskTariff(String riskCode, String packageCode, boolean isRT) {
        String tariffCode = RiskCatalog.tariffCodeFor(riskCode, packageCode);
        List<InsCentrumAirTariffEntity> tariffs = tariffRepository.findByTariffCodeAndRiskRiskCode(tariffCode, riskCode);
        if (tariffs.isEmpty()) {
            throw new IllegalArgumentException("Tariff not found for risk " + riskCode + " and tariffCode " + tariffCode);
        }
        return amount(tariffs.get(0), isRT);
    }

    private BigDecimal tariffSum(int policyGroup, List<String> tariffCodes, boolean isRT) {
        return tariffRepository.findByPolicyGroupAndTariffCodeIn(policyGroup, tariffCodes).stream()
                .map(tariff -> amount(tariff, isRT))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal amount(InsCentrumAirTariffEntity tariff, boolean isRT) {
        return isRT ? tariff.getRoundTripSum() : tariff.getOneWaySum();
    }

    private static void addIfPositive(List<PolicyGroupCalculation> groups, int policyGroup, BigDecimal premium) {
        if (premium.compareTo(BigDecimal.ZERO) > 0) {
            groups.add(new PolicyGroupCalculation(policyGroup, premium));
        }
    }
}
