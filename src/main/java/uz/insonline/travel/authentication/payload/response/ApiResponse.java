package uz.insonline.travel.authentication.payload.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.insonline.travel.authentication.payload.enums.ResponseEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ApiResponse")
public class ApiResponse {
    private String message;
    private int result;

    private Object data;

    public ApiResponse(String message, int result) {
        this.message = message;
        this.result = result;
    }

    public ApiResponse(Object data) {
        this.message = ResponseEnum.SUCCESS.getText();
        this.result = 0;
        this.data = data;
    }
}
