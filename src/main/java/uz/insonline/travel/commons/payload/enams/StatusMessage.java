package uz.insonline.travel.commons.payload.enams;

import lombok.Getter;

@Getter
public enum StatusMessage {
    INVALID_INPUT_DATA("Invalid input data", -410),
    SERVER_ERROR("Server encountered an error", -500),
    AUTHENTICATION_FAILED("Authentication failed", -403),
    PERMISSION_DENIED("Permission denied", -401),
    RESOURCE_NOT_FOUND("Requested resource not found", -404),
    DUPLICATE_RESOURCE("Duplicate resource found", -405),
    PROVIDER_NOT_RESPONDING("Provider not responding", -550),
    CLIENT_ERROR("Client error", -400);

    private final String message;
    private final Integer errorCode;

    StatusMessage(String message, Integer errorCode) {
        this.message = message;
        this.errorCode = errorCode;
    }
}
