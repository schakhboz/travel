package uz.insonline.travel.Provider.payload.reponse;

import lombok.*;
import uz.insonline.travel.commons.payload.response.ApiResponseAll;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PersonDriverSummaryResponseDto extends ApiResponseAll {
    private String pinfl;
    private String last_name;
    private String first_name;
    private String middle_name;
    private String last_name_eng;
    private String first_name_eng;
    private String region_id;
    private String district_id;
    private String address;
    private String birth_date;
    private String license_number;
    private String license_seria;
    private String issue_date;
}
