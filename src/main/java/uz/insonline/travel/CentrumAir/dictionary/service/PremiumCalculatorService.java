package uz.insonline.travel.CentrumAir.dictionary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.insonline.travel.CentrumAir.config.CentrumAirProperties;
import uz.insonline.travel.CentrumAir.dictionary.dto.*;
import uz.insonline.travel.CentrumAir.domain.ProductSelection;
import uz.insonline.travel.CentrumAir.domain.RiskCatalog;
import uz.insonline.travel.CentrumAir.entity.InsCentrumAirRiskEntity;
import uz.insonline.travel.CentrumAir.entity.InsCentrumAirTariffEntity;
import uz.insonline.travel.CentrumAir.error.CentrumAirApiException;
import uz.insonline.travel.CentrumAir.repository.InsCentrumAirRiskRepository;
import uz.insonline.travel.CentrumAir.repository.InsCentrumAirTariffRepository;
import uz.insonline.travel.CentrumAir.service.PolicyCalculationService;
import uz.insonline.travel.CentrumAir.service.ValidationService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Калькулятор премии для партнёров: та же арифметика, что и при выпуске полисов (ТЗ п. 4–5),
 * но с расшифровкой — из каких тарифных ставок и множителей сложилась премия каждого полиса
 * и как она распределена по рискам.
 * <p>
 * Ничего не сохраняет и не выпускает: это витрина расчёта.
 */
@Service
@RequiredArgsConstructor
public class PremiumCalculatorService {

    private static final DecimalFormat AMOUNT = amountFormat();

    private final InsCentrumAirTariffRepository tariffRepository;
    private final InsCentrumAirRiskRepository riskRepository;
    private final PolicyCalculationService calculationService;
    private final CentrumAirProperties properties;

    @Transactional(readOnly = true)
    public CalculationResponse calculate(CalculationRequest request) {
        validate(request);

        ProductSelection products = ProductSelection.of(request.products());
        boolean roundTrip = "RT".equalsIgnoreCase(request.routeType());
        int passengers = request.passengerCount();
        String packageCode = products.packageCode();
        BigDecimal eurRate = properties.getEurRate();

        List<PolicyPremiumDto> policies = new ArrayList<>();
        Long insuredDays = null;

        // П0: ВЗР — суточная ставка в EUR за каждого застрахованного.
        if (products.has(ProductSelection.TRAVEL)) {
            insuredDays = insuredDays(request);
            BigDecimal dailyRate = tariffSum(0, ProductSelection.TRAVEL, roundTrip);
            BigDecimal amount = dailyRate
                    .multiply(BigDecimal.valueOf(insuredDays))
                    .multiply(eurRate)
                    .multiply(BigDecimal.valueOf(passengers));

            PremiumComponentDto component = new PremiumComponentDto(
                    ProductSelection.TRAVEL, ProductSelection.TRAVEL, dailyRate, "EUR",
                    passengers, "passengers",
                    "%s EUR × %d days × %s × %d passengers = %s".formatted(
                            format(dailyRate), insuredDays, format(eurRate), passengers, format(amount)),
                    amount);
            policies.add(policy(0, amount, List.of(component), products, packageCode, roundTrip));
        }

        // П1: авиа-пакет на пассажира, доп. багаж за место, питомец за особь.
        List<PremiumComponentDto> groupOne = new ArrayList<>();
        if (packageCode != null) {
            groupOne.add(perUnit("PACKAGE", packageCode, 1, packageCode, roundTrip, passengers, "passengers"));
        }
        if (products.quantity(ProductSelection.ADDON_BAGGAGE) > 0) {
            groupOne.add(perUnit(ProductSelection.ADDON_BAGGAGE, ProductSelection.ADDON_BAGGAGE, 1,
                    ProductSelection.ADDON_BAGGAGE, roundTrip,
                    products.quantity(ProductSelection.ADDON_BAGGAGE), "baggage items"));
        }
        if (products.quantity(ProductSelection.ANIMAL) > 0) {
            groupOne.add(perUnit(ProductSelection.ANIMAL, ProductSelection.ANIMAL, 1,
                    ProductSelection.ANIMAL, roundTrip,
                    products.quantity(ProductSelection.ANIMAL), "pets"));
        }
        addIfPositive(policies, 1, groupOne, products, packageCode, roundTrip);

        // П2 и П3 — только вместе с авиа-пакетом.
        if (packageCode != null) {
            addIfPositive(policies, 2,
                    List.of(perUnit("PACKAGE", packageCode, 2, packageCode, roundTrip, passengers, "passengers")),
                    products, packageCode, roundTrip);
            addIfPositive(policies, 3,
                    List.of(perUnit("PACKAGE", packageCode, 3, packageCode, roundTrip, passengers, "passengers")),
                    products, packageCode, roundTrip);
        }

        BigDecimal total = policies.stream()
                .map(PolicyPremiumDto::premiumAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CalculationResponse.success(eurRate, insuredDays, total, policies);
    }

    /** Ставка × количество единиц: пассажиров, мест багажа или питомцев. */
    private PremiumComponentDto perUnit(String component, String tariffCode, int policyGroup,
                                        String rateCode, boolean roundTrip, int units, String unitName) {
        BigDecimal rate = tariffSum(policyGroup, rateCode, roundTrip);
        BigDecimal amount = rate.multiply(BigDecimal.valueOf(units));
        return new PremiumComponentDto(component, tariffCode, rate, "UZS", units, unitName,
                "%s × %d %s = %s".formatted(format(rate), units, unitName, format(amount)), amount);
    }

    private void addIfPositive(List<PolicyPremiumDto> policies, int policyGroup,
                               List<PremiumComponentDto> components, ProductSelection products,
                               String packageCode, boolean roundTrip) {
        BigDecimal amount = components.stream()
                .map(PremiumComponentDto::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            policies.add(policy(policyGroup, amount, components, products, packageCode, roundTrip));
        }
    }

    private PolicyPremiumDto policy(int policyGroup, BigDecimal premium, List<PremiumComponentDto> components,
                                    ProductSelection products, String packageCode, boolean roundTrip) {
        List<String> riskCodes = RiskCatalog.risksForGroup(policyGroup, products);
        Map<String, BigDecimal> shares =
                calculationService.splitPremiumByRisk(premium, riskCodes, packageCode, roundTrip);

        List<RiskShareDto> risks = riskCodes.stream()
                .map(riskCode -> riskShare(riskCode, shares.get(riskCode), premium))
                .toList();

        return new PolicyPremiumDto(policyGroup, policyName(policyGroup), premium, components, risks);
    }

    private RiskShareDto riskShare(String riskCode, BigDecimal riskPremium, BigDecimal policyPremium) {
        List<InsCentrumAirRiskEntity> risks = riskRepository.findAllByRiskCode(riskCode);
        InsCentrumAirRiskEntity risk = risks.isEmpty() ? null : risks.get(0);
        BigDecimal insuredSum = risks.stream()
                .map(InsCentrumAirRiskEntity::getInsuranceSum)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal share = policyPremium.signum() == 0 ? BigDecimal.ZERO
                : riskPremium.multiply(BigDecimal.valueOf(100)).divide(policyPremium, 2, RoundingMode.HALF_UP);

        return new RiskShareDto(riskCode,
                risk == null ? null : risk.getTitle(),
                risk == null ? null : risk.getClassId(),
                insuredSum,
                risk == null ? null : risk.getCurrency(),
                riskPremium, share);
    }

    /** Срок страхования по ВЗР: для Шенгена +15 дней к последнему прилёту (ТЗ п. 3.1). */
    private static long insuredDays(CalculationRequest request) {
        LocalDate arrival = request.arrivalDate() != null ? request.arrivalDate() : request.returnDate();
        LocalDate end = Boolean.TRUE.equals(request.isSchengen())
                ? arrival.plusDays(15)
                : request.returnDate();
        return ChronoUnit.DAYS.between(request.departureDate(), end);
    }

    private BigDecimal tariffSum(int policyGroup, String tariffCode, boolean roundTrip) {
        List<InsCentrumAirTariffEntity> tariffs =
                tariffRepository.findByPolicyGroupAndTariffCodeIn(policyGroup, List.of(tariffCode));
        if (tariffs.isEmpty()) {
            throw CentrumAirApiException.validation(
                    "Tariff for product " + tariffCode + " in policy group " + policyGroup + " is not configured");
        }
        return tariffs.stream()
                .map(tariff -> roundTrip ? tariff.getRoundTripSum() : tariff.getOneWaySum())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validate(CalculationRequest request) {
        if (request == null) {
            throw CentrumAirApiException.validation("Request body cannot be null");
        }
        if (request.routeType() == null
                || !("OW".equalsIgnoreCase(request.routeType()) || "RT".equalsIgnoreCase(request.routeType()))) {
            throw CentrumAirApiException.validation("Field 'routeType' must be OW or RT");
        }
        if (request.passengerCount() == null || request.passengerCount() <= 0) {
            throw CentrumAirApiException.validation("Field 'passengerCount' must be greater than zero");
        }
        if (request.products() == null || request.products().isEmpty()) {
            throw CentrumAirApiException.validation("products block is required and cannot be empty");
        }

        if (ProductSelection.of(request.products()).has(ProductSelection.TRAVEL)) {
            if (request.departureDate() == null || request.returnDate() == null) {
                throw CentrumAirApiException.validation(
                        "Fields 'departureDate' and 'returnDate' are required for the TRAVEL product");
            }
            if (request.returnDate().isBefore(request.departureDate())) {
                throw CentrumAirApiException.validation("'returnDate' must not be earlier than 'departureDate'");
            }
        }

        if (properties.getValidation().isStrictProductRules()) {
            ValidationService.validateProductCombination(request.products(),
                    Boolean.TRUE.equals(request.isInternational()),
                    "RT".equalsIgnoreCase(request.routeType()));
        }
    }

    private static String policyName(int policyGroup) {
        return switch (policyGroup) {
            case 0 -> "P0 - Travel: medical expenses, death of the insured";
            case 1 -> "P1 - Accident, baggage, additional baggage, pet";
            case 2 -> "P2 - Trip cancellation";
            case 3 -> "P3 - Flight delay, loss of documents";
            default -> "P" + policyGroup;
        };
    }

    private static String format(BigDecimal value) {
        return AMOUNT.format(value);
    }

    private static DecimalFormat amountFormat() {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.ROOT);
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator('.');
        return new DecimalFormat("#,##0.##", symbols);
    }
}
