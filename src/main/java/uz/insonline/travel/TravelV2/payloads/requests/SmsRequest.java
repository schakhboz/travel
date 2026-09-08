package uz.insonline.travel.TravelV2.payloads.requests;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SmsRequest implements Serializable {

    String phone;
    String message;
}
