package uz.insonline.travel.Provider.payload.reponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uz.insonline.travel.commons.payload.response.ApiResponseAll;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PersonBirthDateResponseDto")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonBirthDateResponseDto extends ApiResponseAll {
    private String pinfl;
    private String last_name;
    private String first_name;
    private String middle_name;
    private String last_name_eng;
    private String first_name_eng;
    private String gender;
    private String region_id;
    private String district_id;
    private String address;
    private String birth_date;
}
