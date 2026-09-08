package uz.insonline.travel.CentrumAir;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uz.insonline.travel.CentrumAir.config.CentrumAirProperties;
import uz.insonline.travel.CentrumAir.dictionary.dto.CalculationRequest;
import uz.insonline.travel.CentrumAir.dictionary.dto.CalculationResponse;
import uz.insonline.travel.CentrumAir.dictionary.dto.PolicyPremiumDto;
import uz.insonline.travel.CentrumAir.dictionary.service.PremiumCalculatorService;
import uz.insonline.travel.CentrumAir.dto.*;
import uz.insonline.travel.CentrumAir.dto.calculation.PolicyCalculationResult;
import uz.insonline.travel.CentrumAir.dto.calculation.PolicyGroupCalculation;
import uz.insonline.travel.CentrumAir.dto.request.PolicyIssueRequest;
import uz.insonline.travel.CentrumAir.entity.InsCentrumAirRiskEntity;
import uz.insonline.travel.CentrumAir.entity.InsCentrumAirTariffEntity;
import uz.insonline.travel.CentrumAir.error.CentrumAirApiException;
import uz.insonline.travel.CentrumAir.repository.InsCentrumAirRiskRepository;
import uz.insonline.travel.CentrumAir.repository.InsCentrumAirTariffRepository;
import uz.insonline.travel.CentrumAir.service.PolicyCalculationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Калькулятор — витрина того же расчёта, что и выпуск. Тест сравнивает его ответ
 * с {@link PolicyCalculationService}: разъедутся формулы — упадёт сборка.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PremiumCalculatorServiceTest {

    private static final LocalDate DEPARTURE = LocalDate.of(2026, 9, 4);
    private static final LocalDate RETURN = LocalDate.of(2026, 9, 15);

    /** Тарифная матрица примера ТЗ п. 5.4.5, ставки — за единицу. */
    private static final Map<String, BigDecimal> RATES = Map.of(
            "0:TRAVEL", new BigDecimal("2.4"),
            "1:MAXIMUM", new BigDecimal("41600"),
            "1:STANDARD", new BigDecimal("30000"),
            "1:ADDON_BAGGAGE", new BigDecimal("55000"),
            "1:ANIMAL", new BigDecimal("40000"),
            "2:MAXIMUM", new BigDecimal("166600"),
            "2:STANDARD", new BigDecimal("120000"),
            "3:MAXIMUM", new BigDecimal("76300"),
            "3:STANDARD", new BigDecimal("55500")
    );

    @Mock
    private InsCentrumAirTariffRepository tariffRepository;
    @Mock
    private InsCentrumAirRiskRepository riskRepository;

    private final CentrumAirProperties properties = new CentrumAirProperties();
    private PolicyCalculationService calculationService;
    private PremiumCalculatorService calculator;

    @BeforeEach
    void setUp() {
        when(tariffRepository.findByPolicyGroupAndTariffCodeIn(any(), any())).thenAnswer(invocation -> {
            Integer group = invocation.getArgument(0);
            List<String> codes = invocation.getArgument(1);
            return codes.stream()
                    .map(code -> RATES.get(group + ":" + code))
                    .filter(java.util.Objects::nonNull)
                    .map(PremiumCalculatorServiceTest::tariff)
                    .toList();
        });
        // Для разнесения премии по рискам доли равны: проверяется совпадение сумм, а не пропорции.
        when(tariffRepository.findByTariffCodeAndRiskRiskCode(anyString(), anyString()))
                .thenReturn(List.of(tariff(BigDecimal.ONE)));
        when(riskRepository.findAllByRiskCode(anyString())).thenAnswer(invocation -> List.of(risk(invocation.getArgument(0))));

        calculationService = new PolicyCalculationService(tariffRepository);
        calculator = new PremiumCalculatorService(tariffRepository, riskRepository, calculationService, properties);
    }

    @Test
    void fullPurchaseMatchesTheIssuePathPolicyByPolicy() {
        List<ProductDto> products = List.of(
                new ProductDto("MAXIMUM", null),
                new ProductDto("TRAVEL", null),
                new ProductDto("ADDON_BAGGAGE", 2),
                new ProductDto("ANIMAL", 1));

        assertMatchesIssuePath(products, "RT");
    }

    @Test
    void packageOnlyOneWayMatchesTheIssuePath() {
        assertMatchesIssuePath(List.of(new ProductDto("STANDARD", null)), "OW");
    }

    @Test
    void breaksDownTheTravelPremiumIntoRateDaysAndPassengers() {
        CalculationResponse response = calculator.calculate(request(
                List.of(new ProductDto("MAXIMUM", null), new ProductDto("TRAVEL", null)), "RT"));

        assertEquals(11L, response.insuredDays());
        PolicyPremiumDto travel = response.policies().get(0);
        assertEquals(0, travel.policyGroup());
        // 2.4 EUR × 11 дней × 13 500 × 2 пассажира
        assertEquals(0, new BigDecimal("712800.0").compareTo(travel.premiumAmount()));
        assertEquals("TRAVEL", travel.components().get(0).component());
        assertTrue(travel.components().get(0).formula().contains("11 days"));
        assertEquals(1, travel.risks().size());
        assertEquals("TRAVEL", travel.risks().get(0).riskCode());
    }

    @Test
    void rejectsTravelOnOneWayRoute() {
        CentrumAirApiException error = assertThrows(CentrumAirApiException.class,
                () -> calculator.calculate(request(List.of(
                        new ProductDto("MAXIMUM", null), new ProductDto("TRAVEL", null)), "OW")));
        assertTrue(error.getMessage().contains("round-trip"));
    }

    @Test
    void rejectsAddonBaggageWithoutPackage() {
        CentrumAirApiException error = assertThrows(CentrumAirApiException.class,
                () -> calculator.calculate(request(List.of(new ProductDto("ADDON_BAGGAGE", 2)), "RT")));
        assertTrue(error.getMessage().contains("aviation package"));
    }

    private void assertMatchesIssuePath(List<ProductDto> products, String routeType) {
        CalculationResponse calculated = calculator.calculate(request(products, routeType));
        PolicyCalculationResult issued =
                calculationService.calculatePolicies(issueRequest(products, routeType), properties.getEurRate());

        assertEquals(issued.policyGroups().size(), calculated.policies().size());
        for (PolicyGroupCalculation group : issued.policyGroups()) {
            PolicyPremiumDto policy = calculated.policies().stream()
                    .filter(item -> item.policyGroup() == group.policyGroup())
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Calculator lost policy group " + group.policyGroup()));
            assertEquals(0, group.premiumAmount().compareTo(policy.premiumAmount()),
                    "Premium of policy group " + group.policyGroup() + " differs from the issue path");
        }
        assertEquals(0, issued.totalPremiumAmount().compareTo(calculated.totalPremiumAmount()));
    }

    private static CalculationRequest request(List<ProductDto> products, String routeType) {
        return new CalculationRequest(routeType, true, false, 2, DEPARTURE, RETURN, RETURN, products);
    }

    /** Тот же набор данных в контракте выпуска: две одинаковые даты сегментов и два пассажира. */
    private static PolicyIssueRequest issueRequest(List<ProductDto> products, String routeType) {
        OffsetDateTime departure = DEPARTURE.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime back = RETURN.atStartOfDay().atOffset(ZoneOffset.UTC);
        RouteDto route = new RouteDto(routeType, true, false, List.of(
                new SegmentDto(1, "C6-101", "TAS", "DXB", departure, departure.plusHours(3)),
                new SegmentDto(2, "C6-102", "DXB", "TAS", back, back.plusHours(3))));

        return new PolicyIssueRequest("PNR123", departure.minusDays(1), "WEB",
                BigDecimal.ZERO, "UZS", route, "RU", products,
                List.of(new TransactionDto(1, "tx-1")),
                PolicyRequests.insurant(),
                List.of(PolicyRequests.passenger(), PolicyRequests.passenger()));
    }

    private static InsCentrumAirTariffEntity tariff(BigDecimal rate) {
        InsCentrumAirTariffEntity tariff = new InsCentrumAirTariffEntity();
        tariff.setOneWaySum(rate);
        tariff.setRoundTripSum(rate);
        return tariff;
    }

    private static InsCentrumAirRiskEntity risk(String riskCode) {
        InsCentrumAirRiskEntity risk = new InsCentrumAirRiskEntity();
        risk.setRiskCode(riskCode);
        risk.setTitle("Risk " + riskCode);
        risk.setClassId("1");
        risk.setInsuranceSum(new BigDecimal("105000000"));
        risk.setCurrency("UZS");
        return risk;
    }
}
