package uz.insonline.travel.CentrumAir;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uz.insonline.travel.CentrumAir.config.CentrumAirProperties;
import uz.insonline.travel.CentrumAir.dto.*;
import uz.insonline.travel.CentrumAir.dto.calculation.PolicyCalculationResult;
import uz.insonline.travel.CentrumAir.dto.calculation.PolicyGroupCalculation;
import uz.insonline.travel.CentrumAir.dto.request.PolicyIssueRequest;
import uz.insonline.travel.CentrumAir.error.CentrumAirApiException;
import uz.insonline.travel.CentrumAir.error.CentrumAirErrorCode;
import uz.insonline.travel.CentrumAir.service.PolicyCalculationService;
import uz.insonline.travel.CentrumAir.service.ValidationService;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Сверка премии с тарифной матрицей: заявленная авиакомпанией сумма не может быть выше расчёта INSON.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ValidationServiceTest {

    private static final List<ProductDto> PRODUCTS = List.of(new ProductDto("STANDARD", null));

    @Mock
    private PolicyCalculationService calculationService;

    private final CentrumAirProperties properties = new CentrumAirProperties();
    private ValidationService validationService;

    private void calculationReturns(String total) {
        when(calculationService.calculatePolicies(any(), any())).thenReturn(new PolicyCalculationResult(
                new BigDecimal(total), List.of(new PolicyGroupCalculation(1, new BigDecimal(total)))));
        validationService = new ValidationService(properties, calculationService);
    }

    @Test
    void rejectsRequestWhereReportedPremiumExceedsTheTariff() {
        calculationReturns("120000");

        CentrumAirApiException error = assertThrows(CentrumAirApiException.class,
                () -> validationService.validate(request("130000")));

        assertEquals(CentrumAirErrorCode.PREMIUM_MISMATCH, error.getErrorCode());
        assertEquals("Calculated premium 120000 is less than totalPremiumAmount 130000 sent in the request",
                error.getMessage());
    }

    @Test
    void acceptsRequestWhereReportedPremiumEqualsTheTariff() {
        calculationReturns("120000");

        assertDoesNotThrow(() -> validationService.validate(request("120000")));
    }

    @Test
    void acceptsRequestWhereReportedPremiumIsBelowTheTariff() {
        calculationReturns("120000");

        assertDoesNotThrow(() -> validationService.validate(request("100000")));
    }

    @Test
    void allowsRoundingWithinTheConfiguredTolerance() {
        properties.getValidation().setPremiumTolerance(new BigDecimal("100"));
        calculationReturns("120000");

        assertDoesNotThrow(() -> validationService.validate(request("120100")));
        assertThrows(CentrumAirApiException.class, () -> validationService.validate(request("120101")));
    }

    @Test
    void skipsTheCheckForNonUzsPremium() {
        calculationReturns("120000");

        assertDoesNotThrow(() -> validationService.validate(request("130000", "USD")));
    }

    @Test
    void skipsTheCheckWhenDisabled() {
        properties.getValidation().setCheckPremium(false);
        calculationReturns("120000");

        assertDoesNotThrow(() -> validationService.validate(request("130000")));
    }

    private static PolicyIssueRequest request(String totalPremium) {
        return request(totalPremium, "UZS");
    }

    private static PolicyIssueRequest request(String totalPremium, String currency) {
        PolicyIssueRequest base = PolicyRequests.issueRequest("PNR123", PRODUCTS);
        return new PolicyIssueRequest(base.pnr(), base.paymentTime(), base.salesChannel(),
                new BigDecimal(totalPremium), currency, base.route(), base.language(), base.products(),
                base.transactions(), base.insurant(), base.passengers());
    }
}
