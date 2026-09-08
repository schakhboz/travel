package uz.insonline.travel.Travel.payload.request.reference;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(name = "TravelMultiDayType")
public class TravelMultiDayType {
    private Integer id;
    private Integer days;
    private String name;
}
