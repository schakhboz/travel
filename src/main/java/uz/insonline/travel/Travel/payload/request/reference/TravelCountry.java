package uz.insonline.travel.Travel.payload.request.reference;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "TravelCountry")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TravelCountry {
    Integer id;
    String name;
    String sp_code;
    Integer isInSchengen;
    List<TravelProgram> programs;
}
