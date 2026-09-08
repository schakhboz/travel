package uz.insonline.travel.Inbound.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.OptBoolean;

@Data
@Schema(name = "InboundTraveler", description = "Данные путешественника")
public class InboundTravelerRequest {

    @Schema(example = "John", description = "Имя туриста")
    @NotBlank(message = "Имя туриста обязательно")
    @Size(min = 2, max = 50, message = "Имя должно быть от 2 до 50 символов")
    @Pattern(regexp = "^[A-Za-z '-]+$", message = "Имя должно содержать только латинские буквы")
    private String firstName;

    @Schema(example = "Doe", description = "Фамилия туриста")
    @NotBlank(message = "Фамилия туриста обязательна")
    @Size(min = 2, max = 50, message = "Фамилия должна быть от 2 до 50 символов")
    @Pattern(regexp = "^[A-Za-z '-]+$", message = "Фамилия должна содержать только латинские буквы")
    private String lastName;

    @Schema(example = "Michael", description = "Отчество (необязательно)")
    @Size(max = 50, message = "Отчество слишком длинное (макс 50 символов)")
    private String middleName;

    @NotNull(message = "Дата рождения туриста обязательна")
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "uuuu-MM-dd", lenient = OptBoolean.FALSE)
    @Schema(example = "1985-10-15", description = "Дата рождения")

    @Past(message = "Дата рождения туриста должна быть в прошлом")
    private LocalDate birthDate;

    @Schema(example = "FA", description = "Серия паспорта")
    @NotBlank(message = "Серия паспорта туриста обязательна")
    @Size(min = 1, max = 10, message = "Серия паспорта - 2 символа")
    private String passportSeries;

    @Schema(example = "12345678", description = "Номер паспорта")
    @NotBlank(message = "Номер паспорта туриста обязателен")
    @Size(min = 7, max = 10, message = "Номер паспорта должен быть от 7 до 10 символов")
    private String passportNumber;

    @Schema(example = "1", description = "ID Гражданства")
    @NotNull(message = "Гражданство туриста обязательно")
    @Min(value = 1, message = "Некорректный ID гражданства")
    @JsonProperty("citizenship_id")
    @JsonAlias({"citizenship", "citizenship_id", "citizenshipId"})
    private Long citizenshipId;

    @Schema(example = "998909876543", description = "Телефон")
    @Size(max = 20, message = "Телефон слишком длинный (макс 20 символов)")
    private String phone;

    @Schema(example = "1", description = "Пол: 1 (М) или 2 (Ж)")
    @NotNull(message = "Пол туриста обязателен")
    @Min(value = 1, message = "Пол: 1 (М) или 2 (Ж)")
    @Max(value = 2, message = "Пол: 1 (М) или 2 (Ж)")
    private Integer gender;

    @Schema(example = "tourist@example.com", description = "Email (необязательно, если не указан - используется email заявителя)")
    @Email(message = "Некорректный email туриста")
    @Size(max = 100, message = "Email слишком длинный (макс 100 символов)")
    private String email;
}