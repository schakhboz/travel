package uz.insonline.travel.commons.payload.response;

public class ErrorResponse {

    private Error error;

    public ErrorResponse(String message, int errorCode) {
        this.error = new Error(message, errorCode);
    }

    public Error getError() {
        return error;
    }

    public static class Error {
        private String message;
        private int errorCode;

        public Error(String message, int errorCode) {
            this.message = message;
            this.errorCode = errorCode;
        }

        public String getMessage() {
            return message;
        }

        public int getErrorCode() {
            return errorCode;
        }
    }
}
