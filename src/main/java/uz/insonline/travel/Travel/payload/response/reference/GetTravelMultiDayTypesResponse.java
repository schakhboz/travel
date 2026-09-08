package uz.insonline.travel.Travel.payload.response.reference;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uz.insonline.travel.Travel.payload.request.reference.TravelMultiDayType;
import uz.insonline.travel.commons.payload.response.ApiResponseAll;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(name="GetTravelMultiDayTypesResponse")
public class GetTravelMultiDayTypesResponse extends ApiResponseAll {
    private List<TravelMultiDayType> data;
}
