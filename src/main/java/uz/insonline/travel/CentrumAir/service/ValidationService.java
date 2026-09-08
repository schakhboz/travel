package uz.insonline.travel.CentrumAir.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uz.insonline.travel.CentrumAir.config.CentrumAirProperties;
import uz.insonline.travel.CentrumAir.domain.ProductSelection;
import uz.insonline.travel.CentrumAir.dto.InsurantDto;
import uz.insonline.travel.CentrumAir.dto.PassengerDto;
import uz.insonline.travel.CentrumAir.dto.ProductDto;
import uz.insonline.travel.CentrumAir.dto.RouteDto;
import uz.insonline.travel.CentrumAir.dto.SegmentDto;
import uz.insonline.travel.CentrumAir.dto.calculation.PolicyCalculationResult;
import uz.insonline.travel.CentrumAir.dto.request.PolicyIssueRequest;
import uz.insonline.travel.CentrumAir.error.CentrumAirApiException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Валидация запроса на выпуск (ТЗ п. 7.6): обязательные поля и форматы, даты вылета не раньше оплаты,
 * допустимость комбинации продуктов и сверка премии с тарифной матрицей.
 * Проверка на дубли живёт в {@code IdempotencyService} — она опирается на ключ идемпотентности.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^998\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    private final CentrumAirProperties properties;
    private final PolicyCalculationService calculationService;

    public void validate(PolicyIssueRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }

        validateRequired(request.pnr(), "pnr");
        validateRequired(request.paymentTime(), "paymentTime");
        validateRequired(request.totalPremiumAmount(), "totalPremiumAmount");
        validateRequired(request.premiumCurrency(), "premiumCurrency");

        validateRoute(request.route());
        validateProducts(request.products());
        validateInsurant(request.insurant());
        validatePassengers(request.passengers());

        validateDeparturesAfterPayment(request);
        if (properties.getValidation().isStrictProductRules()) {
            validateProductCombination(request);
        }
        if (properties.getValidation().isCheckPremium()) {
            validatePremium(request);
        }
    }

    private void validateRoute(RouteDto route) {
        validateRequired(route, "route");
        validateRequired(route.routeType(), "route.routeType");
        validateRequired(route.isInternational(), "route.isInternational");
        validateRequired(route.isSchengen(), "route.isSchengen");

        if (route.segments() == null || route.segments().isEmpty()) {
            throw new IllegalArgumentException("route.segments must contain at least one segment");
        }

        for (int i = 0; i < route.segments().size(); i++) {
            SegmentDto segment = route.segments().get(i);
            String prefix = "route.segments[" + i + "].";
            validateRequired(segment.segmentOrder(), prefix + "segmentOrder");
            validateRequired(segment.flightNumber(), prefix + "flightNumber");
            validateRequired(segment.departureAirport(), prefix + "departureAirport");
            validateRequired(segment.arrivalAirport(), prefix + "arrivalAirport");
            validateRequired(segment.departureTime(), prefix + "departureTime");
            validateRequired(segment.arrivalTime(), prefix + "arrivalTime");
        }
    }

    private void validateProducts(List<ProductDto> products) {
        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("products block is required and cannot be empty");
        }

        for (int i = 0; i < products.size(); i++) {
            ProductDto product = products.get(i);
            String prefix = "products[" + i + "].";
            validateRequired(product.productCode(), prefix + "productCode");

            if (ProductSelection.ADDON_BAGGAGE.equals(product.productCode())
                    || ProductSelection.ANIMAL.equals(product.productCode())) {
                if (product.quantity() == null || product.quantity() <= 0) {
                    throw new IllegalArgumentException(
                            prefix + "quantity is required and must be > 0 for code: " + product.productCode());
                }
            }
        }
    }

    private void validateInsurant(InsurantDto insurant) {
        validateRequired(insurant, "insurant");
        validatePhone(insurant.phone(), "insurant.phone");
        validateEmail(insurant.email(), "insurant.email");
        validateRequired(insurant.residentType(), "insurant.residentType");
        validateRequired(insurant.pinfl(), "insurant.pinfl");
        validateRequired(insurant.passportSeries(), "insurant.passportSeries");
        validateRequired(insurant.passportNumber(), "insurant.passportNumber");
        validateRequired(insurant.firstName(), "insurant.firstName");
        validateRequired(insurant.lastName(), "insurant.lastName");
        validateRequired(insurant.birthDate(), "insurant.birthDate");
        validateRequired(insurant.gender(), "insurant.gender");
        validateRequired(insurant.citizenshipId(), "insurant.citizenshipId");
    }

    private void validatePassengers(List<PassengerDto> passengers) {
        if (passengers == null || passengers.isEmpty()) {
            throw new IllegalArgumentException("passengers block is required and cannot be empty");
        }

        for (int i = 0; i < passengers.size(); i++) {
            PassengerDto passenger = passengers.get(i);
            String prefix = "passengers[" + i + "].";

            validateRequired(passenger.passportSeries(), prefix + "passportSeries");
            validateRequired(passenger.passportNumber(), prefix + "passportNumber");
            validateRequired(passenger.firstName(), prefix + "firstName");
            validateRequired(passenger.lastName(), prefix + "lastName");
            validateRequired(passenger.gender(), prefix + "gender");
            validateRequired(passenger.birthDate(), prefix + "birthDate");
            validateRequired(passenger.residentType(), prefix + "residentType");
            validatePhone(passenger.phone(), prefix + "phone");
            validateEmail(passenger.email(), prefix + "email");
            validateRequired(passenger.citizenshipId(), prefix + "citizenshipId");
        }
    }

    /** Полис действует с даты оплаты, поэтому вылет не может быть раньше неё (ТЗ п. 7.6.1). */
    private void validateDeparturesAfterPayment(PolicyIssueRequest request) {
        OffsetDateTime paymentTime = request.paymentTime();
        for (SegmentDto segment : request.route().segments()) {
            if (segment.departureTime().isBefore(paymentTime)) {
                throw CentrumAirApiException.validation(
                        "Segment " + segment.flightNumber() + " departs before the payment time");
            }
            if (segment.arrivalTime().isBefore(segment.departureTime())) {
                throw CentrumAirApiException.validation(
                        "Segment " + segment.flightNumber() + " arrives before its departure");
            }
        }
    }

    /** Допустимость комбинации продуктов (ТЗ п. 7.6.2). */
    private void validateProductCombination(PolicyIssueRequest request) {
        ProductSelection products = ProductSelection.of(request.products());
        RouteDto route = request.route();

        if (countOf(request.products(), ProductSelection.TRAVEL) > 1) {
            throw CentrumAirApiException.validation("Only one TRAVEL product per booking is allowed");
        }
        if (packageCount(request.products()) > 1) {
            throw CentrumAirApiException.validation("Only one aviation package per booking is allowed");
        }
        if (products.has(ProductSelection.TRAVEL)
                && !(Boolean.TRUE.equals(route.isInternational()) && PolicyCalculationService.isRoundTrip(request))) {
            throw CentrumAirApiException.validation("TRAVEL is available for international round-trip routes only");
        }
        if (products.packageCode() == null
                && (products.has(ProductSelection.ANIMAL) || products.has(ProductSelection.ADDON_BAGGAGE))) {
            throw CentrumAirApiException.validation(
                    "ANIMAL and ADDON_BAGGAGE require an aviation package in the same booking");
        }
    }

    /** Сверка премии авиакомпании с расчётом по тарифной матрице в пределах допуска (ТЗ п. 7.6.4). */
    private void validatePremium(PolicyIssueRequest request) {
        if (!"UZS".equalsIgnoreCase(request.premiumCurrency())) {
            log.info("Premium check skipped: request currency is {}", request.premiumCurrency());
            return;
        }

        PolicyCalculationResult calculation = calculationService.calculatePolicies(request, properties.getEurRate());
        BigDecimal expected = calculation.totalPremiumAmount();
        if (expected.signum() == 0) {
            return;
        }

        BigDecimal deviation = request.totalPremiumAmount().subtract(expected).abs()
                .divide(expected, 4, RoundingMode.HALF_UP);
        if (deviation.compareTo(properties.getValidation().getPremiumTolerance()) > 0) {
            throw CentrumAirApiException.validation("totalPremiumAmount " + request.totalPremiumAmount()
                    + " does not match the tariff matrix amount " + expected);
        }
    }

    private static long countOf(List<ProductDto> products, String productCode) {
        return products.stream()
                .filter(product -> productCode.equalsIgnoreCase(product.productCode()))
                .count();
    }

    private static long packageCount(List<ProductDto> products) {
        return products.stream()
                .filter(product -> ProductSelection.of(List.of(product)).packageCode() != null)
                .count();
    }

    private void validateRequired(Object value, String fieldName) {
        if (value == null || (value instanceof String str && !StringUtils.hasText(str))) {
            throw new IllegalArgumentException("Field '" + fieldName + "' is required");
        }
    }

    private void validatePhone(String phone, String fieldName) {
        validateRequired(phone, fieldName);
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("Field '" + fieldName + "' must be 12 digits starting with 998");
        }
    }

    private void validateEmail(String email, String fieldName) {
        validateRequired(email, fieldName);
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Field '" + fieldName + "' has invalid email format");
        }
    }
}
