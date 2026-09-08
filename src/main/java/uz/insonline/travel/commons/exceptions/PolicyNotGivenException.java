package uz.insonline.travel.commons.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
@EqualsAndHashCode(callSuper = true)
@Data
public class PolicyNotGivenException extends RuntimeException {
    private int result;
    private String resultMessage;

    public PolicyNotGivenException(int result, String resultMessage) {
        super(resultMessage);
        this.result = result;
        this.resultMessage = resultMessage;
    }
}