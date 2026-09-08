package uz.insonline.travel.Travel.payload.response;

public record FileResponse(int code, String message, byte[] bytes, boolean isError, String fileName) {
    public FileResponse(int code, String message, byte[] bytes, boolean isError) {
        this(code, message, bytes, isError, "");
    }
}