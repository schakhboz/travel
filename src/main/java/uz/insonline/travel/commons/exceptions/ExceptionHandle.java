package uz.insonline.travel.commons.exceptions;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import uz.insonline.travel.authentication.payload.response.ApiResponseAll;
import uz.insonline.travel.commons.config.RestAuthenticationEntryPoint;
import uz.insonline.travel.commons.payload.response.ErrorResponse;
import uz.insonline.travel.commons.telegram.service.TelegramService;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.NoSuchElementException;

@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionHandle extends ResponseEntityExceptionHandler {

    private static final Logger logger = LogManager.getLogger(ExceptionHandle.class);
    private final TelegramService telegramService;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        logger.error("Endpoint not found: ", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponseAll(-1,
                        "Endpoint not found: " + ex.getResourcePath()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponseAll> handleCustomValidationException(ValidationException ex, WebRequest request) {
        logger.error("Validation exception: ", ex);
        ApiResponseAll body = new ApiResponseAll(400, ex.getMessage());
        var response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);

        try {
            telegramService.sendErrorToTelegram(ex, request, response);
        } catch (Exception telegramEx) {
            logger.error("Failed to send error to Telegram: ", telegramEx);
        }
        return response;
    }

    //    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
//    public ResponseEntity<?> handleUnsupportedMedia(HttpMediaTypeNotSupportedException ex) {
//        logger.error("An error occurred: ", ex);
//        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("Content-Type 'application/json' is required");
//    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handle(MethodArgumentTypeMismatchException ex) {

        String message = String.format(
                "Parameter '%s' is invalid. Value: '%s'. Expected type: %s",
                ex.getName(),
                ex.getValue(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"
        );

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(message, -1));
    }

    @ExceptionHandler(PolicyNotFoundException.class)
    public ResponseEntity<ApiResponseAll> handlePolicyNotFoundException(PolicyNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiResponseAll(ex.getResult(), ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public NotCreateExceptionDto handleForbiddenException(ForbiddenException ex) {
        logger.error("An error occurred: ", ex);
        return new NotCreateExceptionDto(403, ex.getMessage());
    }


    @ExceptionHandler(NotCreateException.class)
    public NotCreateExceptionDto handleNotCreatException(NotCreateException ex) {
        logger.error("An error occurred: ", ex);
        return new NotCreateExceptionDto(400, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public void ProviderException(Exception ex, HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
        System.out.println(ex.getMessage());
        logger.error("An error occurred: ", ex);
        ApiResponseAll body = new ApiResponseAll(-1, ex.toString());
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        new com.fasterxml.jackson.databind.ObjectMapper().writeValue(response.getWriter(), body);
        response.getWriter().flush();
        try {
            telegramService.sendErrorToTelegram(ex, new org.springframework.web.context.request.ServletWebRequest(request), new ResponseEntity<>(body, HttpStatus.BAD_REQUEST));
        } catch (Exception telegramEx) {
            logger.error("Failed to send error to Telegram: ", telegramEx);
        }
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        java.util.List<String> errorMessages = new java.util.ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errorMessages.add(fieldName + ": " + message);
        });
        String errorMessage = "Validation failed. " + String.join("; ", errorMessages);
        logger.error("handle Method Argument Not Valid :", ex);
        NotCreateExceptionDto dto = new NotCreateExceptionDto(HttpStatus.BAD_REQUEST.value(), errorMessage);
        var response = new ResponseEntity<>(dto, HttpStatus.BAD_REQUEST);
        try {
            telegramService.sendErrorToTelegram(ex, request, response);
        } catch (Exception telegramEx) {
            logger.error("Failed to send error to Telegram: ", telegramEx);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((Object) dto);
    }

    @ExceptionHandler(jakarta.validation.ValidationException.class)
    public ResponseEntity<ApiResponseAll> handleValidationException(jakarta.validation.ValidationException ex) {
        Throwable cause = ex.getCause();
        String message = ex.getMessage();
        if (cause != null && cause.getMessage() != null) {
            message = cause.getMessage();
        }
        logger.error("jakarta validation error: ", ex);
        return ResponseEntity.badRequest().body(new ApiResponseAll(-1, "Validation error: " + message));
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ApiResponseAll> handleConstraintViolationException(jakarta.validation.ConstraintViolationException ex) {
        java.util.List<String> errorMessages = new java.util.ArrayList<>();
        ex.getConstraintViolations().forEach(violation -> {
            errorMessages.add(violation.getPropertyPath() + ": " + violation.getMessage());
        });
        String errorMessage = "Constraint violation. " + String.join("; ", errorMessages);
        logger.error("jakarta constraint violation error: ", ex);
        return ResponseEntity.badRequest().body(new ApiResponseAll(-1, errorMessage));
    }

    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex, @NotNull HttpHeaders headers, @NotNull HttpStatusCode status, @NotNull WebRequest request) {
        String message = "Path variable [" + ex.getVariableName() + "] is required";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseAll(-1, message));
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, @NotNull HttpHeaders headers, @NotNull HttpStatusCode status, @NotNull WebRequest request) {
        String message = "This request wait [" + ex.getMethod() + "] HttpMethod";
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(new ApiResponseAll(-1, message));
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, @NotNull HttpHeaders headers, @NotNull HttpStatusCode status, @NotNull WebRequest request) {
        String message = "Request [" + ex.getParameterName() + "] is required";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseAll(-1, message));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, @NotNull HttpHeaders headers, @NotNull HttpStatusCode status, @NotNull WebRequest request) {
        String message;
        if (ex.getMessage() != null && ex.getMessage().contains("body is missing")) {
            message = "Body is missing, request body is required";
        } else {
            Throwable cause = ex.getCause();
            if (cause instanceof com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException unrecognized) {
                message = "Unknown property: " + unrecognized.getPropertyName();
            } else if (cause instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException invalidFormat) {
                String path = invalidFormat.getPath().stream()
                        .map(com.fasterxml.jackson.databind.JsonMappingException.Reference::getFieldName)
                        .filter(java.util.Objects::nonNull)
                        .collect(java.util.stream.Collectors.joining("."));
                message = "Invalid format for field '" + path + "': " + invalidFormat.getValue();
            } else if (cause instanceof com.fasterxml.jackson.core.JsonParseException parseException) {
                message = "Incorrect JSON format";
                var location = parseException.getLocation();
                if (location != null) {
                    message += " (line " + location.getLineNr() + ")";
                }
            } else {
                message = "Incorrect JSON format";
            }
        }
        ApiResponseAll body = new ApiResponseAll(-1, message);
        var response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        try {
            telegramService.sendErrorToTelegram(ex, request, response);
        } catch (Exception telegramEx) {
            logger.error("Failed to send error to Telegram: ", telegramEx);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((Object) body);
    }

    /*
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, @NotNull HttpHeaders headers, @NotNull HttpStatusCode status, @NotNull WebRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ex.getMessage().contains(" body is missing")
                        ? new ApiResponseAll(-1, "Error: Body is missing, request Body is Required")
                        : new ApiResponseAll(-1, "Error: Message Not Readable: " + ex.getMessage())
        );
    }
    */

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, @NotNull HttpHeaders headers, @NotNull HttpStatusCode status, @NotNull WebRequest request) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(
                new ApiResponseAll(-1,
                        "Error: Not Supported Media Type: " + "Content-Type 'application/json' is required!"));
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex, @NotNull HttpHeaders headers, @NotNull HttpStatusCode status, @NotNull WebRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ApiResponseAll(-1, "Error: Missing Request Part: " +
                        "Request part: [" + ex.getRequestPartName() + "] missing"
                )
        );
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(@NotNull NoHandlerFoundException ex, @NotNull HttpHeaders headers, @NotNull HttpStatusCode status, @NotNull WebRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found");
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, @NotNull HttpHeaders headers, @NotNull HttpStatusCode statusCode, @NotNull WebRequest request) {
        ApiResponseAll responseBody = new ApiResponseAll(-1,
                "Some internal Exception " +
                        ex.getMessage());
        var response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body((Object) responseBody);
        try {
            telegramService.sendErrorToTelegram(ex, request, response);
        } catch (Exception telegramEx) {
            logger.error("Failed to send error to Telegram: ", telegramEx);
        }
        return response;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public void handleIllegalArgumentException(IllegalArgumentException ex, HttpServletResponse response) throws java.io.IOException {
        ApiResponseAll body = new ApiResponseAll(-1, ex.getMessage());
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        new com.fasterxml.jackson.databind.ObjectMapper().writeValue(response.getWriter(), body);
        response.getWriter().flush();
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException e) {
        String not_found_entity = "";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponseAll(-1,
                        "Error: Entity Not Found " +
                                "Element in Table [" + not_found_entity + "] not found"
                )
        );
    }

    @ExceptionHandler({SQLException.class, UncategorizedSQLException.class,
            DataAccessException.class, RuntimeException.class})
    public ResponseEntity<Object> handleSqlException(Exception e, WebRequest request) {

        Throwable rootCause = NestedExceptionUtils.getMostSpecificCause(e);

        if (rootCause instanceof SQLException sqlEx) {
            int errorCode = Math.abs(sqlEx.getErrorCode());

            if (errorCode >= 20000 && errorCode <= 20999) {
                String message = extractOracleMessage(sqlEx.getMessage());

                ApiResponseAll body = new ApiResponseAll(400, message);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
            }
        }

        String rawMsg = e.getMessage() != null ? e.getMessage() : "";
        String exceptionPrefix = "";

        if (rawMsg.contains("foreign key")) {
            exceptionPrefix = "Element has relation";
        } else if (rawMsg.contains("duplicate")) {
            exceptionPrefix = "Duplicate Key Value";
        } else {
            exceptionPrefix = "Database Exception";
        }

        String cleanMessage = extractOracleMessage(rawMsg);

        ApiResponseAll body = new ApiResponseAll(-1, exceptionPrefix + " : " + cleanMessage);

        var response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body((Object) body);

        try {
            telegramService.sendErrorToTelegram(e, request, response);
        } catch (Exception telegramEx) {
            logger.error("Failed to send error to Telegram: ", telegramEx);
        }

        return response;
    }

    private String extractOracleMessage(String message) {
        if (message == null || message.isBlank()) {
            return "";
        }

        String cleanStr = message.replaceAll("^ORA-\\d+:\\s*", "");

        int nextOraIndex = cleanStr.indexOf("ORA-");
        if (nextOraIndex != -1) {
            cleanStr = cleanStr.substring(0, nextOraIndex);
        }

        return cleanStr.trim();
    }
    @ExceptionHandler(NoSuchElementException.class)
    public void handleNoSuchElement(NoSuchElementException e, HttpServletResponse response) throws java.io.IOException {
        ApiResponseAll body = new ApiResponseAll(-1, "No Such Element: " + e.getMessage());
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        new com.fasterxml.jackson.databind.ObjectMapper().writeValue(response.getWriter(), body);
        response.getWriter().flush();
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleNullPointException(NullPointerException e, WebRequest request) {
        ApiResponseAll body = new ApiResponseAll(-1,
                "Null Point Exception:" +
                        e.getMessage());
        var response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body((Object) body);
        try {
            telegramService.sendErrorToTelegram(e, request, response);
        } catch (Exception telegramEx) {
            logger.error("Failed to send error to Telegram: ", telegramEx);
        }
        return response;
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleOfFileSizeLimit(FileSizeLimitExceededException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponseAll(-1,
                        "File size is big then 15 Mb: " +
                                exception.getMessage()
                )
        );
    }

    @ExceptionHandler
    public ResponseEntity<?> notFoundUsername(UsernameNotFoundException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ApiResponseAll(-1, "Username not found exception:" +
                        e.getMessage())
        );
    }

    @ExceptionHandler
    public ResponseEntity<?> parseException(ParseException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ApiResponseAll(-1, "Some parse exceptionm:" +
                        e.getMessage())
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public NotCreateExceptionDto handleAccessDeniedException(AccessDeniedException ex) {
        logger.error("Access denied exception: ", ex);
        return new NotCreateExceptionDto(405, ex.getMessage());
    }

    @ExceptionHandler(CreationException.class)
    public void handleContractCreationErrors(CreationException e, HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
        response.setStatus(e.getHttpStatus().value());
        response.setContentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        new com.fasterxml.jackson.databind.ObjectMapper().writeValue(response.getWriter(), e.getDto());
        response.getWriter().flush();
        try {
            telegramService.sendErrorToTelegram(e, new org.springframework.web.context.request.ServletWebRequest(request), new ResponseEntity<>(e.getDto(), e.getHttpStatus()));
        } catch (Exception telegramEx) {
            logger.error("Failed to send error to Telegram: ", telegramEx);
        }
    }

    @ExceptionHandler
    public void badCredentialsException(AuthenticationException e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        authenticationEntryPoint.commence(request, response, e);
    }
}
