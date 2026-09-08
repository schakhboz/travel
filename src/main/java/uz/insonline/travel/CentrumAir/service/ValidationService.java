package uz.insonline.travel.CentrumAir.service;

import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uz.insonline.travel.CentrumAir.dto.*;
import uz.insonline.travel.CentrumAir.dto.request.PolicyIssueRequest;

@Service
public class ValidationService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^998\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

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
            validateRequired(segment.segmentOrder(),     prefix + "segmentOrder");
            validateRequired(segment.flightNumber(),     prefix + "flightNumber");
            validateRequired(segment.departureAirport(), prefix + "departureAirport");
            validateRequired(segment.arrivalAirport(),   prefix + "arrivalAirport");
            validateRequired(segment.departureTime(),    prefix + "departureTime");
            validateRequired(segment.arrivalTime(),      prefix + "arrivalTime");
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

            if ("ADDON_BAGGAGE".equals(product.productCode()) || "ANIMAL".equals(product.productCode())) {
                if (product.quantity() == null || product.quantity() <= 0) {
                    throw new IllegalArgumentException(prefix + "quantity is required and must be > 0 for code: " + product.productCode());
                }
            }
        }
    }

    private void validateInsurant(InsurantDto insurant) {
        validateRequired(insurant,                  "insurant");
        validatePhone(insurant.phone(),             "insurant.phone");
        validateEmail(insurant.email(),             "insurant.email");
        validateRequired(insurant.residentType(),   "insurant.residentType");
        validateRequired(insurant.pinfl(),          "insurant.pinfl");
        validateRequired(insurant.passportSeries(), "insurant.passportSeries");
        validateRequired(insurant.passportNumber(), "insurant.passportNumber");
        validateRequired(insurant.firstName(),      "insurant.firstName");
        validateRequired(insurant.lastName(),       "insurant.lastName");
        validateRequired(insurant.birthDate(),      "insurant.birthDate");
        validateRequired(insurant.gender(),         "insurant.gender");
        validateRequired(insurant.citizenshipId(),  "insurant.citizenshipId");
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
            validateRequired(passenger.firstName(),      prefix + "firstName");
            validateRequired(passenger.lastName(),       prefix + "lastName");
            validateRequired(passenger.gender(),         prefix + "gender");
            validateRequired(passenger.birthDate(),      prefix + "birthDate");
            validateRequired(passenger.residentType(),   prefix + "residentType");
            validatePhone(passenger.phone(),             prefix + "phone");
            validateEmail(passenger.email(),             prefix + "email");
            validateRequired(passenger.citizenshipId(),  prefix + "citizenshipId");
        }
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