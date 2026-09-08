package uz.insonline.travel.commons.annotation.TravelV2;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import uz.insonline.travel.TravelV2.payloads.requests.ContractRequestV2;
import uz.insonline.travel.authentication.entity.UserEntity;

import java.util.List;

public class ContractRequestV2Validator implements ConstraintValidator<TravelRequestValidator, ContractRequestV2> {

    /**
     * 204 (USA),
     * 55  (Canada),
     * 240 (Japan),
     * 254 (Australia),
     * 101 (New Zealand);
     */
    private static final List<String> RESTRICTED_COUNTRIES_FOR_SIMPLE_GOLD = List.of("US", "CA", "JP", "AU", "NZ");

    @Override
    public boolean isValid(ContractRequestV2 dto, ConstraintValidatorContext context) {
        if (dto == null) return true;
        boolean valid = true;

        valid &= validateDetails(dto, context);
        valid &= validateApplicant(dto, context);
        valid &= validateTravelers(dto, context);
        return valid;
    }

    private boolean validateApplicant(ContractRequestV2 dto, ConstraintValidatorContext context) {

        ContractRequestV2.ApplicantDTO applicant = dto.getApplicant();
        if (applicant == null) {
            addViolation(context, "Applicant can not be null", "applicant");
            return false;
        }

        ContractRequestV2.PersonDTO person = applicant.getPerson();
        if (person == null) {
            addViolation(context, "Applicant person can not be null", "applicant.person");
            return false;
        }

        boolean valid = true;
        boolean skipValidation = isPrestigeUser();
        boolean isResidentApplicant = person.getResident() != null && person.getResident() == 1;
        boolean isNonResidentApplicant = person.getResident() != null && person.getResident() == 2;

        if (!skipValidation && isResidentApplicant && person.getPhone() == null) {
            addViolation(context, "Applicant phone can not be null", "applicant.person.phone");
            valid = false;
        }

        if (!skipValidation && isResidentApplicant && isInvalidPhone(person)) {
            addViolation(context, "Applicant phone is not valid. The number must be in format 998XXXXXXXXX", "applicant.person.phone");
            valid = false;
        }

        if (isResidentApplicant && person.getPassport_series() != null && !person.getPassport_series().matches("^[A-Z]{2}$")) {
            addViolation(context, "Applicant passport series must be 2 capital letters", "applicant.person.passport_series");
            valid = false;
        }

        if (isNonResidentApplicant) {

            if (person.getFirstName() == null) {
                addViolation(context, "If non-resident applicant firstname can not be null", "applicant.person.first_name");
                valid = false;
            }

            if (person.getLastName() == null) {
                addViolation(context, "If non-resident applicant lastname can not be null", "applicant.person.last_name");
                valid = false;
            }

            if (!skipValidation && person.getEmail() == null) {
                addViolation(context, "If non-resident applicant email can not be null", "applicant.person.email");
                valid = false;
            }

            if (!skipValidation && person.getEmail() != null) {
                boolean invalidEmail = isInvalidEmail(person);
                if (invalidEmail) {
                    addViolation(context, "applicant.email is incorrect. example: test@gmail.com", "applicant.person.email");
                    return false;
                }
            }

            if (person.getGender() == null) {
                addViolation(context, "applicant.gender can not be null for nonresidents", "applicant.person.gender");
            }

            if (person.getCitizenship() == null) {
                addViolation(context, "If non-resident applicant citizenship can not be null", "applicant.person.citizenship");
                valid = false;
            }
        }
        return valid;
    }

    private boolean validateTravelers(ContractRequestV2 dto, ConstraintValidatorContext context) {
        ContractRequestV2.ApplicantDTO applicant = dto.getApplicant();
        if (applicant == null) return true;

        List<ContractRequestV2.TravelerData> travelers = dto.getTravelers();

        if (travelers == null || travelers.isEmpty()) {
            addViolation(context, "travelers: travelers is required", "travelers");
            return false;
        }
        if (applicant.getIs_applicant_traveler() != null && applicant.getIs_applicant_traveler() != 1 && travelers.size() > 6) {
            addViolation(context, "Travelers can not be more than 6", "travelers");
            return false;
        }

        for (int i = 0; i < travelers.size(); i++) {
            int travelerOrder = i + 1;

            ContractRequestV2.TravelerData travelerData = travelers.get(i);
            if (travelerData == null) {
                addViolation(context, "traveler can not be null", "travelers[" + travelerOrder + "]");
                return false;
            }

            ContractRequestV2.PersonDTO travelerPerson = travelerData.getPerson();
            if (travelerPerson == null) {
                addViolation(context, "traveler can not be null", "travelers[" + travelerOrder + "]");
                return false;
            }
            if (travelerPerson.getResident() == null) {
                addViolation(context, "travelers[" + travelerOrder + "]: If non-resident traveler so traveler.is_resident is required", "travelers");
                return false;
            }


            if (travelerPerson.getResident() == 2) {
                if (travelerPerson.getCitizenship() == null) {
                    addViolation(context, "travelers[" + travelerOrder + "]: If non-resident traveler so traveler.citizenship is required", "travelers");
                    return false;
                }
                if (travelerPerson.getFirstName() == null || travelerPerson.getFirstName().isBlank()) {
                    addViolation(context, "travelers[" + travelerOrder + "]: If non-resident traveler so traveler.first_name is required", "travelers");
                    return false;
                }
                if (travelerPerson.getLastName() == null || travelerPerson.getLastName().isBlank()) {
                    addViolation(context, "travelers[" + travelerOrder + "]: If non-resident traveler so traveler.last_name is required", "travelers");
                    return false;
                }
            }
        }
        return true;
    }

    private boolean validateDetails(ContractRequestV2 dto, ConstraintValidatorContext context) {
        ContractRequestV2.DetailsDTO details = dto.getDetails();
        if (details == null) {
            addViolation(context, "details can not be null", "details");
            return false;
        }

        List<String> countries = details.getCountries();
        if (countries == null || countries.isEmpty()) {
            addViolation(context, "countries is required", "details.countries");
            return false;
        }

        for (String country : countries) {
            if (country == null || country.isBlank()) {
                addViolation(
                        context,
                        "countries must not be contains null or empty",
                        "details.countries"
                );
                return false;
            }
        }
        
        if (countries.size() > 10) {
            addViolation(context, "maximum 10 countries allowed", "details.countries");
            return false;
        }

        if (details.getTravel_type() == null || (details.getTravel_type() != 0 && details.getTravel_type() != 1)) {
            addViolation(context, "travel_type must be 0 (Single) or 1 (Multi)", "details.travel_type");
            return false;
        } else {
            if (details.getTravel_type() == 0 && (details.getTravel_days() == null || details.getTravel_days() <= 0)) {
                addViolation(context, "travel_days is required and must be > 0 when travel_type is 0", "details.travel_days");
                return false;
            }
            if (details.getTravel_type() == 1 && (details.getMulti_day_type() == null || details.getMulti_day_type() <= 0)) {
                addViolation(context, "multi_day_type is required and must be > 0 when travel_type is 1", "details.multi_day_type");
                return false;
            }
        }

        Integer activityId = details.getActivity_type();
        if (activityId == null) {
            addViolation(context, "activity_type is required", "details.activity_type");
            return false;
        }

        if (details.getGroup_type() == null) {
            addViolation(context, "group_type is required", "details.group_type");
            return false;
        }

        Integer programId = details.getProgram_type();
        if (programId == null) {
            addViolation(context, "program_type is required", "details.program_type");
            return false;
        }

        if (programId != null && programId == 6) {
            if (activityId == null || activityId != 2) {
                addViolation(context, "program_type = 6 (Simple Gold) available only for activity_type = 2 (Работа)", "details.activity_type");
                return false;
            }
            if (countries.stream().anyMatch(RESTRICTED_COUNTRIES_FOR_SIMPLE_GOLD::contains)) {
                addViolation(context, "program_type = 6 not available for restricted countries [US, CA, JP, AU, NZ]", "details.countries");
                return false;
            }
        }
        return true;
    }

    private static boolean isInvalidEmail(ContractRequestV2.PersonDTO person) {
        String email = person.getEmail();
        if (email == null || email.isBlank()) {
            return true;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return true;
        }
        String domain = email.substring(email.indexOf("@") + 1);
        return domain.matches("\\d+\\.\\d+\\.\\d+\\.\\d+");
    }

    private static boolean isInvalidPhone(ContractRequestV2.PersonDTO person) {
        String phone = person.getPhone();
        if (person.getPhone() == null || person.getPhone().isEmpty() || person.getPhone().isBlank()) {
            return true;
        }
        return (!phone.matches("^998\\d{9}$"));
    }

    private boolean isPrestigeUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        return user.getTbId() == 2544; // PRESTIGE travel
    }

    private static void addViolation(ConstraintValidatorContext context, String message, String field) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
    }

}
