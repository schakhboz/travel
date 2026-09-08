package uz.insonline.travel.commons.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@EqualsAndHashCode(callSuper = true)
@ResponseStatus(HttpStatus.NOT_FOUND)
@Data
public class PolicyNotFoundException extends RuntimeException {
    private int result;
    private String resultMessage;

    public PolicyNotFoundException(int result, String resultMessage) {
        super(resultMessage);
        this.result = result;
        this.resultMessage = resultMessage;
    }
}
