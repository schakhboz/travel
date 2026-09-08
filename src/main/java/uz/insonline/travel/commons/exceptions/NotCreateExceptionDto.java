package uz.insonline.travel.commons.exceptions;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NotCreateExceptionDto {

    @Schema(description = "Response code (0 - Transaction completed successfully)", example = "0")
    private int result;

    @Schema(description = "Response information", example = "Successful request processing")
    private String result_message;

    public NotCreateExceptionDto(int result, String result_message) {
        this.result = result;
        this.result_message = result_message;
    }
}
