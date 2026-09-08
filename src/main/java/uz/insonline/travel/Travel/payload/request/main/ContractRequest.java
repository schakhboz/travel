package uz.insonline.travel.Travel.payload.request.main;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import uz.insonline.travel.commons.annotation.TravelV1.ValidContractRequestV1;
import uz.insonline.travel.commons.payload.request.ApiRequest;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidContractRequestV1
@Schema(name = "ContractRequest")
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractRequest extends ApiRequest {

    @Schema(name = "applicant", description = "Information about the applicant (individual or organization) - Either person or organization must be (required)", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotNull(message = "Person data is required")
    private ApplicantDTO applicant;

    @Schema(name = "details", description = "Additional information about the travel (type, program, ...)", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    private DetailsDTO details;

    @Schema(description = "List of travelers. Maximum 6 travelers", requiredMode = Schema.RequiredMode.REQUIRED, name = "travelers")
    @Valid
    private List<TravelerData> travelers;

    @Schema(name = "transactionId", description = "Transaction id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(min = 1, max = 36, message = "Transaction id must be between 1 and 36 characters")
    @JsonProperty("transaction_id")
    @JsonAlias({"transaction_id", "transactionId"})
    private String transactionId;

    @JsonIgnore
    @Builder.Default
    private boolean isApplicantIndividual = false;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "ApplicantDTO")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ApplicantDTO {

        @Schema(description = "The applicant (Individual)")
        @Valid
        private PersonDTO person;

        @Schema(description = "Phone number of the person", example = "998901234567", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Phone number can not be null")
        @NotEmpty(message = "Phone number can not be empty")
        @NotBlank(message = "Phone number can not be blank")
        @Pattern(regexp = "^998[0-9]{9}$", message = "Phone number must be in format 998XXXXXXXXX")
        private String phone;

        @Schema(description = "Is applicant is insured person if yes then (1) else (0)", example = "1")
        @NotNull(message = "Applicant is insured person field is not nullable!")
        @Builder.Default
        private Integer is_applicant_traveler = 1;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @Schema(name = "details")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DetailsDTO {

        @Schema(description = "Contract (policy) start date", example = "2024-05-15", pattern = "yyyy-MM-dd", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @NotNull(message = "startDate can not be null")
        @FutureOrPresent(message = "startDate must be future or present")
        private LocalDate start_date;

        @Schema(description = "Travel type id (api/travel/types)", example = "0/1")
        private Integer travel_type;

        @Schema(description = "Travel activity type id (/api/travel/activities))", example = "1")
        private Integer activity_type;

        @Schema(description = "Travel group type id (/api/travel/groups))", example = "1")
        private Integer group_type;

        @Schema(description = "Travel program type id (/api/travel/programs))", example = "1")
        private Integer program_type;

        @Schema(description = "Number of days during which will be traveling. Can be ignored if travelType = 1 (Многократное путешествие) by default = 2", example = "14")
        @Min(value = 1, message = "travel_days must be at least 1")
        @Max(value = 365, message = "travel_days must not exceed 365")
        @Builder.Default
        private Integer travel_days = 2;

        @Schema(description = "Required if travelType = 1 (Многократное путешествие) by default = 1", example = "6")
        @Builder.Default
        private Integer multi_day_type = 1;

        @Schema(description = "List of counties id or Alpha-2 codes", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "Incorrect value for the field details.countries")
        private List<String> countries;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "TravelerData")
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
    @Schema(name = "PersonDTO")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PersonDTO {

        @Schema(description = "Passport series of the person", example = "AA", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Passport series can not be null")
        @NotBlank(message = "Passport series can not be blank")
        @Pattern(regexp = "^[A-Z]{2}$", message = "Passport series must be 2 capital letters")
        private String passport_series;

        @Schema(description = "Passport number of the person", example = "1234567")
        @NotNull(message = "Passport number can not be null")
        @NotBlank(message = "Passport number can not be blank")
        @Pattern(regexp = "^\\d{7}$", message = "Passport number must be 7 digits")
        private String passport_number;

        @Schema(description = "Person's birth date", example = "2000-12-25", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Birth date cannot be null")
        @Past(message = "Birth date must be in the past")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private LocalDate birth_date;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @Schema(name = "OrganizationDto")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrganizationDto {

        @Schema(description = "Organization name", example = "Inson sug'urta tashkiloti")
        @NotNull(message = "Organization name can not be null")
        @NotEmpty(message = "Organization name can not be empty")
        @NotBlank(message = "Organization name can not be blank")
        private String name;

        @Schema(description = "Organization INN", example = "123456789")
        @NotNull(message = "INN can not be null")
        @NotEmpty(message = "INN can not be empty")
        @NotBlank(message = "INN can not be blank")
        @Pattern(regexp = "^[0-9]{9}$", message = "INN must be 9 digits")
        private String inn;
    }

    public void validateIndividual() {
        if (applicant.person != null) {
            this.isApplicantIndividual = true;
        }
    }

}

