package uz.insonline.travel.TravelV2.payloads.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.*;
import uz.insonline.travel.commons.payload.request.ApiRequest;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Tag(name = "Calculator request")
@Schema(name="calculator")
public class TravelCalculatorRequest extends ApiRequest {

    @Schema(description = "Travel type id (/api/travel/types)", example = "0", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "0,1")
    @NotNull(message = "travel type id can not be null")
    @jakarta.validation.constraints.Min(value = 0, message = "travel_type must be 0 or 1")
    @jakarta.validation.constraints.Max(value = 1, message = "travel_type must be 0 or 1")
    @JsonProperty("travel_type")
    private Integer travel_type;

    @Schema(description = "Travel activity type id (/api/travel/activities)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "0,1,2,4")
    @NotNull(message = "activityTypeId can not be null")
    @jakarta.validation.constraints.Min(value = 0, message = "activity_type must be at least 0")
    @jakarta.validation.constraints.Max(value = 20, message = "activity_type must not exceed 20")
    @JsonProperty("activity_type")
    private Integer activity_type;

    @Schema(description = "Travel group type id (/api/travel/groups)", example = "0", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "0,1")
    @NotNull(message = "groupTypeId can not be null")
    @jakarta.validation.constraints.Min(value = 0, message = "group_type must be at least 0")
    @jakarta.validation.constraints.Max(value = 20, message = "group_type must not exceed 20")
    @JsonProperty("group_type")
    private Integer group_type;

    @Schema(description = "Travel program type id (/api/travel/programs)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "programTypeId can not be null")
    private Integer program_type;

    @Schema(description = "Number of days during which will be traveling. Can be ignored if travelType = 1", example = "14", requiredMode = Schema.RequiredMode.REQUIRED)
    @jakarta.validation.constraints.Min(value = 1, message = "travel_days must be at least 1")
    @jakarta.validation.constraints.Max(value = 365, message = "travel_days must not exceed 365")
    private Integer travel_days;

    @Schema(description = "Multi day traveller mode (/api/travel/multi-day-types). Required if travel_type = 1", example = "3", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer multi_day_type;

    @Schema(description = "List of each traveler's date of births. Maximum 6 travelers", requiredMode = Schema.RequiredMode.REQUIRED, name="travelers")
    @Valid
    @jakarta.validation.constraints.NotEmpty(message = "travelers list cannot be empty")
    @JsonProperty("travelers")
    private List<TravelerCalculatorData> travelers;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class TravelerCalculatorData {
        @Schema(description = "traveler's birth date", example = "2000-01-03", pattern = "yyyy-MM-dd", requiredMode = Schema.RequiredMode.REQUIRED)
        @Past(message = "traveler's birth date must be past")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @NotNull(message = "traveler's birth date can not be null")
        private LocalDate birth_date;
    }

    public void validateRequest() {

        if (travelers.size() > 6) {
            throw new IllegalArgumentException("Maximum 6 travelers are allowed");
        }

        if(travel_type == 1 && multi_day_type == null) {
            throw new IllegalArgumentException("Multi day type can not be null if travel type is 1");
        }

        if (travel_type == 0 && travel_days == null) {
            throw new IllegalArgumentException("travel_days can not be null if travel type is 0");
        }
    }


}
