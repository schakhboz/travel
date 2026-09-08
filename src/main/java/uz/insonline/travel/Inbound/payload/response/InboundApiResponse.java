package uz.insonline.travel.Inbound.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InboundApiResponse {
    private int result;
    private String result_message;
}