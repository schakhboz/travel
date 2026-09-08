package uz.insonline.travel.CentrumAir.error;

import org.springframework.http.HttpStatus;

/**
 * Машиночитаемые коды ошибок API (ТЗ п. 7.6.6). Описание клиенту отдаётся на английском.
 */
public enum CentrumAirErrorCode {

    VALIDATION_ERROR(HttpStatus.BAD_REQUEST),
    DUPLICATE(HttpStatus.CONFLICT),
    IDEMPOTENCY_KEY_REUSE(HttpStatus.UNPROCESSABLE_ENTITY),
    ISSUE_IN_PROGRESS(HttpStatus.CONFLICT),
    POLICY_NOT_FOUND(HttpStatus.NOT_FOUND),
    ISSUE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    CentrumAirErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return name().toLowerCase();
    }
}
