package uz.insonline.travel.CentrumAir.error;

import lombok.Getter;

@Getter
public class CentrumAirApiException extends RuntimeException {

    private final CentrumAirErrorCode errorCode;

    public CentrumAirApiException(CentrumAirErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public static CentrumAirApiException validation(String message) {
        return new CentrumAirApiException(CentrumAirErrorCode.VALIDATION_ERROR, message);
    }
}
