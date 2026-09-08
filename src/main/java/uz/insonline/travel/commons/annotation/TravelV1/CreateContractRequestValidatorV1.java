package uz.insonline.travel.commons.annotation.TravelV1;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotEmpty;
import uz.insonline.travel.Travel.payload.request.main.ContractRequest;

import java.util.List;

public class CreateContractRequestValidatorV1 implements ConstraintValidator<ValidContractRequestV1, ContractRequest> {

    /**
     * 204 (USA),
     * 55  (Canada),
     * 240 (Japan),
     * 254 (Australia),
     * 101 (New Zealand);
     */
    
    private static final List<Integer> RESTRICTED_COUNTRIES_FOR_SIMPLE_GOLD = List.of(204, 55, 240, 254, 101);

    @Override
    public boolean isValid(ContractRequest dto, ConstraintValidatorContext context) {
        if (dto == null) return false;
        boolean valid = true;
        valid &= validateDetails(dto, context);
        valid &= validateApplicantAndTravelers(dto, context);
        return valid;
    }

    private static boolean validateDetails(ContractRequest dto, ConstraintValidatorContext context) {
        ContractRequest.DetailsDTO details = dto.getDetails();
        boolean valid = true;

        if (details == null) {
            addViolation(context, "details is required", "details");
            return false;
        }

        Integer activityId = details.getActivity_type();
        Integer programId = details.getProgram_type();
        @NotEmpty(message = "Incorrect value for the field details.countries") List<String> countries = details.getCountries();

        if (details.getTravel_type() == null || (details.getTravel_type() != 0 && details.getTravel_type() != 1)) {
            addViolation(context, "travel_type must be 0 (Single) or 1 (Multi)", "details.travel_type");
            valid = false;
        } else {
            if (details.getTravel_type() == 0 && (details.getTravel_days() == null || details.getTravel_days() <= 0)) {
                addViolation(context, "travel_days is required and must be > 0 when travel_type is 0", "details.travel_days");
                valid = false;
            }
            if (details.getTravel_type() == 1 && (details.getMulti_day_type() == null || details.getMulti_day_type() <= 0)) {
                addViolation(context, "multi_day_type is required and must be > 0 when travel_type is 1", "details.multi_day_type");
                valid = false;
            }
        }
        
        if (countries != null && countries.size() > 10) {
            addViolation(context, "maximum 10 countries allowed", "details.countries");
            valid = false;
        }
        
        if (activityId == null) {
            addViolation(context, "activity_type is required", "details.activity_type");
            valid = false;
        }
        
        if (details.getGroup_type() == null) {
            addViolation(context, "group_type is required", "details.group_type");
            valid = false;
        }
        
        if (programId == null) {
            addViolation(context, "program_type is required", "details.program_type");
            valid = false;
        }

        if (programId != null && programId == 6) {
            if (activityId == null || activityId != 2) {
                addViolation(context,
                        "program_type = 6 (Simple Gold) available ony for activity_type = 2 (Работа)",
                        "details.activity_type");
                valid = false;
            }
            if (countries != null) {
                for (String country : countries) {
                    if (country == null) {
                        addViolation(context, "Country value in the list cannot be null", "details.countries");
                        valid = false;
                        break;
                    }
                    try {
                        int countryId = Integer.parseInt(country);
                        if (RESTRICTED_COUNTRIES_FOR_SIMPLE_GOLD.contains(countryId)) {
                            addViolation(context,
                                    "program_type = 6 (Simple Gold) available when countries NOT in [204,55,240,254,101] (USA,Canada,Japan,Australia,New Zealand)",
                                    "details.countries");
                            valid = false;
                            break;
                        }
                    } catch (NumberFormatException ignored) {
                        // If it's an Alpha-2 code (e.g., "US"), it won't match the numeric restricted list.
                        // We could add code-based checks here if needed.
                    }
                }
            }
        }
        return valid;
    }

    private static boolean validateApplicantAndTravelers(ContractRequest dto, ConstraintValidatorContext context) {
        boolean valid = true;
        ContractRequest.ApplicantDTO applicant = dto.getApplicant();
        List<ContractRequest.TravelerData> travelers = dto.getTravelers();

        if (applicant == null) {
            addViolation(context, "Applicant can not be null", "applicant");
            return false;
        }
        if (applicant.getIs_applicant_traveler() != null && applicant.getIs_applicant_traveler() != 1
                && travelers != null && !travelers.isEmpty() && travelers.size() > 6) {
            addViolation(context,
                    "Travelers can not be more than 6",
                    "travelers");
            valid = false;
        }
        
        if (travelers == null || travelers.isEmpty()) {
            addViolation(context, "travelers: travelers is required", "travelers");
            valid = false;
        } else {
            for (int i = 0; i < travelers.size(); i++) {
                if (travelers.get(i) == null) {
                    addViolation(context, "Traveler object cannot be null", "travelers[" + i + "]");
                    valid = false;
                    continue;
                }
                if (travelers.get(i).getPerson() == null) {
                    addViolation(context, "Person object in traveler cannot be null", "travelers[" + i + "].person");
                    valid = false;
                }
            }
        }
        return valid;
    }

    private static void addViolation(ConstraintValidatorContext context, String message, String field) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
    }

}