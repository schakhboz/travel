package uz.insonline.travel.commons.telegram.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class TelegramRequest {

    private String message;

}
