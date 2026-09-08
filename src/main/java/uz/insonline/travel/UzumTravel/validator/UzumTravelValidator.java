package uz.insonline.travel.UzumTravel.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.insonline.travel.UzumTravel.payloads.requests.UzumTravelContractRequest;
import uz.insonline.travel.commons.exceptions.ValidationException;
import uz.insonline.travel.commons.payload.request.InsurantDto;
import uz.insonline.travel.commons.payload.request.InsurantOrganizationDto;
import uz.insonline.travel.commons.payload.request.InsurantPersonDto;
import uz.insonline.travel.commons.payload.request.TravelerDto;

import uz.insonline.travel.UzumTravel.service.UzumTravelService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class UzumTravelValidator {

    private final UzumTravelService uzumTravelService;

    public void validate(UzumTravelContractRequest request) {

        if (request.getStartDate() == null) {
            throw new ValidationException("Insurance start date (departure) is required");
        }

        if (request.getEndDate() == null) {
            throw new ValidationException("End date is required");
        }

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new ValidationException("Insurance start date cannot be in the past");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ValidationException("End date cannot be earlier than start date");
        }

        long daysBetween = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        if (daysBetween > 30) {
            throw new ValidationException("Maximum insurance period is 30 days. You specified: " + daysBetween);
        }

        if (request.getInsurant() == null) {
            throw new ValidationException("Insurant data is required");
        }
        validateInsurant(request.getInsurant());

        if (request.getTravelers() == null || request.getTravelers().isEmpty()) {
            throw new ValidationException("Passenger list cannot be empty");
        }

        for (TravelerDto traveler : request.getTravelers()) {
            validateTraveler(traveler);
        }
    }

    private void validateInsurant(InsurantDto insurant) {

        Integer insType = insurant.getInsurantType();
        if (insType == null || (insType != 0 && insType != 1)) {
            throw new ValidationException("Invalid insurantType. Allowed: 0 (Individual), 1 (Organization).");
        }

        if (insType == 0) { // Individual
            if (insurant.getPerson() == null) {
                throw new ValidationException("The 'person' block is required for 'Individual' type.");
            }
            if (insurant.getOrganization() != null) {
                throw new ValidationException("The 'organization' block must be null for 'Individual' type.");
            }
            
            InsurantPersonDto person = insurant.getPerson();
            if (person.getGender() == null || (person.getGender() != 0 && person.getGender() != 1)) {
                throw new ValidationException("Invalid gender for insurant. Allowed: 0 (Female), 1 (Male).");
            }
            if (person.getBirthDate() == null) {
                throw new ValidationException("Birth date is required for the insurant.");
            }
            if (person.getBirthDate().isAfter(LocalDate.now())) {
                throw new ValidationException("Insurant birth date cannot be in the future");
            }
            if (person.getBirthDate().isBefore(LocalDate.of(1900, 1, 1))) {
                throw new ValidationException("Invalid insurant birth date");
            }
            if (person.getCitizenshipId() == null) {
                throw new ValidationException("Citizenship is required for the insurant.");
            }

            String series = person.getPassportSeries();
            String number = person.getPassportNumber();

            if (series == null || number == null) {
                throw new ValidationException("Passport series and number are required for person.");
            }

            // Regex checks for passport
            if (insurant.getResidentType() != null && insurant.getResidentType() == 1) {
                if (!series.matches("^[A-Z]{2}$")) {
                    throw new ValidationException("For resident insurant, passport series must be 2 uppercase Latin letters.");
                }
                if (!number.matches("^\\d{7}$")) {
                    throw new ValidationException("For resident insurant, passport number must be exactly 7 digits.");
                }
            } else {
                if (!series.matches("^[A-Za-z0-9]{1,10}$")) {
                    throw new ValidationException("Invalid passport series for foreign insurant.");
                }
                if (!number.matches("^[A-Za-z0-9]{5,15}$")) {
                    throw new ValidationException("Invalid passport number for foreign insurant.");
                }
            }

            checkLength("Insurant PINFL", person.getPinfl(), 14);
            checkLength("Insurant passport series", person.getPassportSeries(), 10);
            checkLength("Insurant passport number", person.getPassportNumber(), 20);

        } else { // Organization
            if (insurant.getOrganization() == null) {
                throw new ValidationException("The 'organization' block is required for 'Organization' type.");
            }
            if (insurant.getPerson() != null) {
                throw new ValidationException("The 'person' block must be null for 'Organization' type.");
            }
            InsurantOrganizationDto org = insurant.getOrganization();
            if (org.getInn() == null || org.getInn().trim().length() != 9) {
                throw new ValidationException("Organization INN is required (9 digits).");
            }
        }

        Integer resType = insurant.getResidentType();
        if (resType == null || (resType != 1 && resType != 2)) {
            throw new ValidationException("Invalid residentType for insurant. Allowed: 1 (Resident), 2 (Non-resident).");
        }

        if (resType == 1) {
            if (insurant.getPhone() == null || insurant.getPhone().trim().isEmpty()) {
                throw new ValidationException("Phone number is required for resident insurants");
            }

            if (insType == 0) {
                if (insurant.getPerson().getPinfl() == null || insurant.getPerson().getPinfl().trim().length() != 14) {
                    throw new ValidationException("A valid PINFL (14 digits) is required for resident individual insurant");
                }
            }
        } else if (resType == 2) {
            if (insurant.getEmail() == null || insurant.getEmail().trim().isEmpty()) {
                throw new ValidationException("Email is required for non-resident insurant");
            }
        }

        checkLength("Insurant address", insurant.getAddress(), 100);
    }

    private void validateTraveler(TravelerDto traveler) {
        String name = getTravelerName(traveler);

        if (traveler.getFlightNumber() == null || traveler.getFlightNumber().trim().isEmpty()) {
            throw new ValidationException("Flight number is required for passenger " + name);
        }

        if (traveler.getFirstName() == null || traveler.getFirstName().trim().isEmpty()) {
            throw new ValidationException("Passenger first name is required");
        }
        if (traveler.getLastName() == null || traveler.getLastName().trim().isEmpty()) {
            throw new ValidationException("Passenger last name is required");
        }
        if (traveler.getPassportSeries() == null || traveler.getPassportSeries().trim().isEmpty()) {
            throw new ValidationException("Passport series is required");
        }
        if (traveler.getPassportNumber() == null || traveler.getPassportNumber().trim().isEmpty()) {
            throw new ValidationException("Passport number is required");
        }
        if (traveler.getBirthDate() == null) {
            throw new ValidationException("Birth date is required");
        }
        if (traveler.getCitizenshipId() == null) {
            throw new ValidationException("Citizenship is required for passenger " + name);
        }

        if (traveler.getBirthDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Birth date cannot be in the future");
        }
        if (traveler.getBirthDate().isBefore(LocalDate.of(1900, 1, 1))) {
            throw new ValidationException("Invalid birth date");
        }

        // Strict gender check
        if (traveler.getGender() == null || (traveler.getGender() != 0 && traveler.getGender() != 1)) {
            throw new ValidationException("Invalid gender for traveler " + name + ". Allowed: 0 (Female), 1 (Male).");
        }

        // Strict tripType check
        if (traveler.getTripType() == null || (traveler.getTripType() != 1 && traveler.getTripType() != 2)) {
            throw new ValidationException("Invalid tripType for traveler " + name + ". Allowed: 1 (OneWay), 2 (RoundTrip).");
        }

        if (traveler.getRiskIds() == null || traveler.getRiskIds().isEmpty()) {
            throw new ValidationException("For passenger " + name + " risks must be selected");
        }

        for (Integer riskId : traveler.getRiskIds()) {
            if (riskId == 6) {
                throw new ValidationException("Risk ID 6 is not available for Uzum Travel.");
            }
            if (!uzumTravelService.isValidRiskId(riskId)) {
                throw new ValidationException("Invalid risk ID for passenger " + name + ": " + riskId);
            }
        }

        // Условная валидация: Резидент vs Нерезидент
        Integer resType = traveler.getResidentType();
        if (resType == null || (resType != 1 && resType != 2 && resType != 5)) {
            throw new ValidationException("Residency type is not specified or invalid for passenger "
                    + name + ". Allowed: 1, 2, 5.");
        }

        if (resType == 1 || resType == 5) {
            if (traveler.getPinfl() == null || traveler.getPinfl().trim().length() != 14) {
                throw new ValidationException("For passenger (type " + resType + ") " + name
                        + " PINFL is required (14 digits)");
            }
        }

        // Regex checks for traveler passport
        String series = traveler.getPassportSeries();
        String number = traveler.getPassportNumber();

        if (traveler.getResidentType() != null && traveler.getResidentType() == 1) {
            if (!series.matches("^[A-Z]{2}$")) {
                throw new ValidationException("For resident traveler " + name + ", passport series must be 2 uppercase Latin letters.");
            }
            if (!number.matches("^\\d{7}$")) {
                throw new ValidationException("For resident traveler " + name + ", passport number must be exactly 7 digits.");
            }
        } else {
            if (!series.matches("^[A-Za-z0-9]{1,10}$")) {
                throw new ValidationException("Invalid passport series for foreign traveler " + name);
            }
            if (!number.matches("^[A-Za-z0-9]{5,15}$")) {
                throw new ValidationException("Invalid passport number for foreign traveler " + name);
            }
        }

        checkLength("Flight number (passenger " + name + ")", traveler.getFlightNumber(), 20);
        checkLength("PINFL (passenger " + name + ")", traveler.getPinfl(), 14);
        checkLength("Passport series (passenger " + name + ")", traveler.getPassportSeries(), 10);
        checkLength("Passport number (passenger " + name + ")", traveler.getPassportNumber(), 20);
        checkLength("Address (passenger " + name + ")", traveler.getAddress(), 100);
    }

    private void checkLength(String fieldName, String value, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new ValidationException(
                    fieldName + " exceeds the limit (" + value.length() + " of " + maxLength + " characters)");
        }
    }

    private String getTravelerName(TravelerDto t) {
        return (t.getFirstName() != null ? t.getFirstName() : "???") + " "
                + (t.getLastName() != null ? t.getLastName() : "???");
    }

}