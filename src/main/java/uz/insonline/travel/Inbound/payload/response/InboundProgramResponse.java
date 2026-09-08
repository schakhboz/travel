package uz.insonline.travel.Inbound.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InboundProgramResponse {
    private Long id;
    private String name;
    private Double coverage;
    private Double dailyRate;
}