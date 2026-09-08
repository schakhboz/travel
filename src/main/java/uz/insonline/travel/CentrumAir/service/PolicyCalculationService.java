package uz.insonline.travel.CentrumAir.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uz.insonline.travel.CentrumAir.dto.calculation.PolicyCalculationResult;
import uz.insonline.travel.CentrumAir.dto.calculation.PolicyGroupCalculation;
import uz.insonline.travel.CentrumAir.dto.ProductDto;
import uz.insonline.travel.CentrumAir.dto.SegmentDto;
import uz.insonline.travel.CentrumAir.dto.request.PolicyIssueRequest;
import uz.insonline.travel.CentrumAir.entity.InsCentrumAirTariffEntity;
import uz.insonline.travel.CentrumAir.repository.InsCentrumAirTariffRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class PolicyCalculationService {

    private final InsCentrumAirTariffRepository tariffRepository;

    private static final Set<String> PACKAGE_CODES = Set.of("STANDARD", "EXTENDED", "MAXIMUM");

    public PolicyCalculationResult calculatePolicies(PolicyIssueRequest request, BigDecimal eurRate) {
        List<PolicyGroupCalculation> groupCalculations = new ArrayList<>();

        List<SegmentDto> segments = request.route().segments();

        LocalDateTime departureFirst = segments.get(0).departureTime().toLocalDateTime();
        LocalDateTime arrivalLast = segments.get(segments.size() - 1).arrivalTime().toLocalDateTime();

        LocalDate dateDepartureThere = departureFirst.toLocalDate();
        LocalDate dateDepartureReturn = segments.get(segments.size() - 1).departureTime().toLocalDate();

        boolean isRT = "RT".equalsIgnoreCase(request.route().routeType());
        boolean isSchengen = Boolean.TRUE.equals(request.route().isSchengen());
        int passengerCount = request.passengers().size();

        String selectedPackage = getSelectedPackage(request);

        // П0: ВЗР (Travel)
        if (hasProduct(request, "TRAVEL")) {
            LocalDate dateEndP0 = isSchengen ? arrivalLast.toLocalDate().plusDays(15) : dateDepartureReturn;
            long daysForTariff = ChronoUnit.DAYS.between(dateDepartureThere, dateEndP0);

            BigDecimal travelDailyTariff = getTariffSumForGroup(0, List.of("TRAVEL"), isRT);
            BigDecimal travelPremium = travelDailyTariff
                    .multiply(BigDecimal.valueOf(daysForTariff))
                    .multiply(eurRate)
                    .multiply(BigDecimal.valueOf(passengerCount));

            groupCalculations.add(new PolicyGroupCalculation(0, travelPremium));
        }

        //П1: НС + Багаж + Доп багаж + Питомец
        BigDecimal p1Premium = BigDecimal.ZERO;

        // Основной пакет (STANDARD / EXTENDED / MAXIMUM)
        if (selectedPackage != null) {
            BigDecimal p1PackageBase = getTariffSumForGroup(1, List.of(selectedPackage), isRT);
            p1Premium = p1Premium.add(p1PackageBase.multiply(BigDecimal.valueOf(passengerCount)));
        }

        // Доп багаж (ADDON_BAGGAGE)
        int baggageQuantity = getProductQuantity(request, "ADDON_BAGGAGE");
        if (baggageQuantity > 0) {
            BigDecimal baggageTariff = getTariffSumForGroup(1, List.of("ADDON_BAGGAGE"), isRT);
            BigDecimal addonBaggageTotal = baggageTariff.multiply(BigDecimal.valueOf(baggageQuantity));
            p1Premium = p1Premium.add(addonBaggageTotal);
        }

        // Питомец (ANIMAL)
        int animalQuantity = getProductQuantity(request, "ANIMAL");
        if (animalQuantity > 0) {
            BigDecimal animalTariff = getTariffSumForGroup(1, List.of("ANIMAL"), isRT);
            BigDecimal animalTotal = animalTariff.multiply(BigDecimal.valueOf(animalQuantity));
            p1Premium = p1Premium.add(animalTotal);
        }

        if (p1Premium.compareTo(BigDecimal.ZERO) > 0) {
            groupCalculations.add(new PolicyGroupCalculation(1, p1Premium));
        }

        // П2: Отмена поездки (CANCEL)
        if (selectedPackage != null) {
            BigDecimal p2Base = getTariffSumForGroup(2, List.of(selectedPackage), isRT);
            BigDecimal p2Premium = p2Base.multiply(BigDecimal.valueOf(passengerCount));
            if (p2Premium.compareTo(BigDecimal.ZERO) > 0) {
                groupCalculations.add(new PolicyGroupCalculation(2, p2Premium));
            }
        }

        // П3: Задержка рейса + Потеря документов
        if (selectedPackage != null) {
            BigDecimal p3Base = getTariffSumForGroup(3, List.of(selectedPackage), isRT);
            BigDecimal p3Premium = p3Base.multiply(BigDecimal.valueOf(passengerCount));
            if (p3Premium.compareTo(BigDecimal.ZERO) > 0) {
                groupCalculations.add(new PolicyGroupCalculation(3, p3Premium));
            }
        }

        BigDecimal totalPremiumAmount = groupCalculations.stream()
                .map(PolicyGroupCalculation::premiumAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PolicyCalculationResult(totalPremiumAmount, groupCalculations);
    }

    private String getSelectedPackage(PolicyIssueRequest request) {
        if (request.products() == null) return null;
        return request.products().stream()
                .map(ProductDto::productCode)
                .filter(PACKAGE_CODES::contains)
                .findFirst()
                .orElse(null);
    }

    private int getProductQuantity(PolicyIssueRequest request, String productCode) {
        if (request.products() == null) return 0;
        return request.products().stream()
                .filter(p -> productCode.equalsIgnoreCase(p.productCode()))
                .mapToInt(ProductDto::quantity)
                .findFirst()
                .orElse(0);
    }

    private BigDecimal getTariffSumForGroup(Integer policyGroup, List<String> tariffCodes, boolean isRT) {
        if (tariffCodes.isEmpty()) return BigDecimal.ZERO;
        List<InsCentrumAirTariffEntity> tariffs = tariffRepository.findByPolicyGroupAndTariffCodeIn(policyGroup, tariffCodes);

        return tariffs.stream()
                .map(t -> isRT ? t.getRoundTripSum() : t.getOneWaySum())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean hasProduct(PolicyIssueRequest request, String productCode) {
        if (request.products() == null) return false;
        return request.products().stream()
                .anyMatch(p -> productCode.equalsIgnoreCase(p.productCode()));
    }
}
