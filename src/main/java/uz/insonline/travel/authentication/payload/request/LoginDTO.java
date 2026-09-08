package uz.insonline.travel.authentication.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(name="LoginDTO")
public class LoginDTO {


    @Schema(description = "login",example = "login")
    @NotBlank(message = "login must not be empty")
    private String login;

    @Schema(description = "password",example = "password")
    @NotNull(message = "password must not be empty")
    private String password;

    public String getLogin() {
        return login.toUpperCase();
    }

    public String getPassword() {
        return password.toUpperCase();
    }
}
