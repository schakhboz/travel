package uz.insonline.travel.CentrumAir.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uz.insonline.travel.CentrumAir.controller.PolicyController;
import uz.insonline.travel.CentrumAir.dictionary.controller.CalculatorController;
import uz.insonline.travel.CentrumAir.dictionary.controller.DictionaryController;

import java.util.UUID;

/**
 * Ошибки Centrum Air отдаются в формате ТЗ п. 7.6.6: код, описание на английском, идентификатор запроса.
 * Действует только на контроллеры Centrum Air, остальные модули продолжает обслуживать общий обработчик.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = {PolicyController.class, DictionaryController.class, CalculatorController.class})
public class CentrumAirExceptionHandler {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    @ExceptionHandler(CentrumAirApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(CentrumAirApiException ex, HttpServletRequest request) {
        String requestId = requestId(request);
        log.warn("Centrum Air request {} rejected: {} – {}", requestId, ex.getErrorCode().code(), ex.getMessage());
        return ResponseEntity.status(ex.getErrorCode().status())
                .body(ApiErrorResponse.of(ex.getErrorCode(), ex.getMessage(), requestId));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        String requestId = requestId(request);
        log.warn("Centrum Air request {} rejected: {}", requestId, ex.getMessage());
        return ResponseEntity.status(CentrumAirErrorCode.VALIDATION_ERROR.status())
                .body(ApiErrorResponse.of(CentrumAirErrorCode.VALIDATION_ERROR, ex.getMessage(), requestId));
    }

    private String requestId(HttpServletRequest request) {
        String header = request.getHeader(REQUEST_ID_HEADER);
        return header != null && !header.isBlank() ? header : UUID.randomUUID().toString();
    }
}
