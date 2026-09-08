package uz.insonline.travel.authentication.payload.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(name="response")
public class ApiResponseAll {
    @Schema(description = "Response code (0 - Transaction completed successfully)", example = "0")
    private int result;
    @Schema(description = "Response information", example = "Successful request processing")
    private String result_message;

    @JsonIgnore
    private Long anketaId = 0L;


    public ApiResponseAll(int result, String result_message) {
        this.result = result;
        this.result_message = result_message;
    }

    public ApiResponseAll(int result, String result_message, Long anketaId) {
        this.result = result;
        this.result_message = result_message;
        this.anketaId = anketaId;
    }

    public ApiResponseAll(int result, String result_message, String anketaId) {
        this.result = result;
        this.result_message = result_message;
        setAnketaId(anketaId);
    }

    public void setAnketaId(String anketaId) {
        try {
            this.anketaId = Long.parseLong(anketaId);
        } catch (Exception e) {
            this.anketaId = -1L;
        }
    }
}
