package uz.insonline.travel.commons.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@EqualsAndHashCode(callSuper = true)
@ResponseStatus(HttpStatus.BAD_REQUEST)
@Data
public class NotCreateException extends RuntimeException{
    public NotCreateException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotCreateException(String message) {super(message);}

    public NotCreateException(Throwable cause) { super(cause); }
}
