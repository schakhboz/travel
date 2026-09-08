package uz.insonline.travel.commons.payload.response;


import uz.insonline.travel.commons.payload.enams.ResponseEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {
    private String message;
    private int result;

    private Object data;

    public ApiResponse(String message, int result) {
        this.message = message;
        this.result = result;
    }

    public ApiResponse(Object data) {
        this.message= ResponseEnum.SUCCESS.getText();
        this.result=0;
        this.data = data;
    }
}
