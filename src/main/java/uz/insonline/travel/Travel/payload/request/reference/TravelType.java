package uz.insonline.travel.Travel.payload.request.reference;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(name="TravelType")
public class TravelType {
    private Integer id;
    private String name;
}
