package uz.insonline.travel.TravelV2.payloads.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import uz.insonline.travel.commons.annotation.TravelV2.TravelRequestValidator;
import uz.insonline.travel.commons.payload.request.ApiRequest;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TravelRequestValidator
@Schema(name = "contract-v2")
@EqualsAndHashCode(callSuper = true)
public class ContractRequestV2 extends ApiRequest {

    @Valid
    @Schema(name = "applicant", description = "Information about the applicant (individual) - Either person or organization must be (required)", requiredMode = Schema.RequiredMode.REQUIRED)
    private ApplicantDTO applicant;

    @Valid
    @Schema(name = "details", description = "Additional information about the travel (type, program, ...)", requiredMode = Schema.RequiredMode.REQUIRED)
    private DetailsDTO details;

    @Valid
    @Schema(name = "travelers", description = "List of travelers. Maximum 6 travelers", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<TravelerData> travelers;

    @JsonIgnore
    @Builder.Default
    private boolean isApplicantIndividual = false;

    @Schema(description = "Transaction id for e-agent")
    @Size(min = 1, max = 36, message = "Transaction id must be between 1 and 36 characters")
    private String transactionId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "applicant")
    public static class ApplicantDTO {

        @Valid
        @NotNull(message = "Person data is required")
        @Schema(description = "The applicant (Individual)", name = "person")
        private PersonDTO person;

        @Builder.Default
//        @NotNull(message = "Applicant is insured person field is not nullable!")
        @Min(value = 0, message = "Is applicant insured person if yes then (1) else (0)")
        @Max(value = 1, message = "Is applicant insured person if yes then (1) else (0)")
        @Schema(description = "Is applicant insured person if yes then (1) else (0) | by default = 1", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        private Integer is_applicant_traveler = 1;

    }

    @Data
    @Builder
    @AllArgsConstructor
    @Schema(name = "details")
    public static class DetailsDTO {

        @Schema(description = "Contract (policy) start date", example = "2024-05-15", pattern = "yyyy-MM-dd", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @NotNull(message = "startDate can not be null")
        @FutureOrPresent(message = "startDate must be future or present")
        private LocalDate start_date;

        @NotNull(message = "travel_type is required")
        @Schema(description = "Travel type id (api/travel/types)", example = "0/1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer travel_type;

        @Schema(description = "Travel activity type id (/api/travel/activities))", example = "1")
        private Integer activity_type;

        @NotNull(message = "group_type is required")
        @Schema(description = "Travel group type id (/api/travel/groups))", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer group_type;

        @Schema(description = "Travel program type id (/api/travel/programs))", example = "1")
        private Integer program_type;

        @Schema(description = "Number of days during which will be traveling. Can be ignored if travelType = 1 (Многократное путешествие) | by default = 2", example = "14")
        @Min(value = 1, message = "travel_days must be at least 1")
        @Max(value = 365, message = "travel_days must not exceed 365")
        @Builder.Default
        private Integer travel_days = 2;

        @Schema(description = "Required if travelType = 1 (Многократное путешествие) | by default = 1", example = "6")
        @Builder.Default
        private Integer multi_day_type = 1;

        @Schema(
                description = "Список стран. Принимает ID (например, 204) или Alpha-2 коды (например, 'US').",
                example = "[\"204\", \"CA\", \"FR\"]",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotEmpty(message = "Incorrect value for the field details.countries")
        private List<String> countries;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "traveler")
    public static class TravelerData {

        @Schema(description = "The applicant (Individual)", name = "person")
        @Valid
        private PersonDTO person;

        public TravelerData(PersonDTO dto) {
            this.person = dto;
        }

    }

    @Data
    @Builder
    @AllArgsConstructor
    @Schema(name = "person")
    public static class PersonDTO {

        @Schema(description = "If person is non-resident so firstName is required", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @JsonProperty("first_name")
        private String firstName;

        @Schema(description = "If person is non-resident so lastName is required", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @JsonProperty("last_name")
        private String lastName;

        @Builder.Default
        @JsonProperty("middle_name")
        @Schema(description = "If person is non-resident so middleName is XXX by default", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        private String middleName = "XXX";

        @Schema(description = "Phone number of the person. If residentType=2 so the field is not required", example = "998901234567", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
//        @Pattern(regexp = "^998[0-9]{9}$", message = "Phone number must be in format 998XXXXXXXXX")
        private String phone;

        @Schema(description = "If person is non-resident so email is required | not required for travelers for both cases", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Email(message = "Enter correct email, example: test@gmail.com")
        private String email;

        @Schema(description = "If person is non-resident so gender is required, Male -> (1) Female -> (0)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
//        @Min(value = 0, message = "Gender must be 0 (female) or 1 (male)")
//        @Max(value = 1, message = "Gender must be 0 (female) or 1 (male)")
//        @NotNull(message = "Gender can not be null")
        private Integer gender;

        @Schema(description = "If person is non-resident then enter citizenship", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        private Integer citizenship;

        @Schema(description = "If the Person is resident so '1' else '2'", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "is_resident field is required!")
        @Min(value = 1, message = "person.is_resident must be 1 or 2")
        @Max(value = 2, message = "person.is_resident must be 1 or 2")
        @JsonProperty("is_resident")
        private Integer resident;

        @Schema(description = "Passport series of the person", example = "AA", requiredMode = Schema.RequiredMode.REQUIRED)
//        @NotNull(message = "Passport series can not be null")
        @NotBlank(message = "Passport series is required")
        private String passport_series;

        @Schema(description = "Passport number of the person", example = "1234567", requiredMode = Schema.RequiredMode.REQUIRED)
//        @NotNull(message = "Passport number can not be null")
        @NotBlank(message = "Passport number is required")
        private String passport_number;

        @Schema(description = "Person's birth date", example = "2000-12-25", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Birth date cannot be null")
        @Past(message = "Birth date must be in the past")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private LocalDate birth_date;
    }


}

