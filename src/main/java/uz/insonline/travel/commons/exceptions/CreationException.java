package uz.insonline.travel.commons.exceptions;

import lombok.*;
import org.springframework.http.HttpStatus;
import uz.insonline.travel.commons.payload.response.ApiResponseAll;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreationException extends RuntimeException {
    private ApiResponseAll dto;
    private HttpStatus httpStatus;
}
