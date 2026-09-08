package uz.insonline.travel.Inbound.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.OptBoolean;
import java.util.List;

@Data
@Schema(name = "InboundCreateRequest", description = "Модель создания заявки (v2)")
public class InboundCreateRequest {

    @Valid
    @NotNull(message = "Детали поездки обязательны")
    @Schema(description = "Детали контракта (даты, программа)")
    private ContractDetails details;

    @Valid
    @NotNull(message = "Данные заявителя обязательны")
    @Schema(description = "Данные заявителя (Физ или Юр лицо)")
    private ApplicantBlock applicant;

    @Valid
    @Size(max = 50, message = "Максимум 50 туристов в одной заявке")
    private List<InboundTravelerRequest> travelers;

    @AssertTrue(message = "Список путешественников не может быть пустым")
    private boolean isTravelersValid() {
        boolean isPerson = applicant != null && applicant.getType() != null && applicant.getType() == 0;
        boolean ownerIsTraveler = isPerson && Boolean.TRUE.equals(applicant.getIsApplicantTraveler());

        if (ownerIsTraveler) {
            return true; // Даже если массив пуст, заявитель пойдет как турист
        }
        return travelers != null && !travelers.isEmpty();
    }

    @Data
    @Schema(name = "ContractDetails")
    public static class ContractDetails {
        @Schema(description = "ID Программы (1=Standart, 2=Comfort)", example = "1")
        @NotNull(message = "programId обязателен")
        @Min(value = 1, message = "programId должен быть больше 0")
        @Max(value = 2, message = "programId недолжен быть больше 2")
        private Long programId;

        @Schema(description = "ID Цели поездки", example = "5")
        @NotNull(message = "activityId обязателен")
        @Min(value = 0, message = "activityId не может быть отрицательным")
        private Long activityId;

        @Schema(description = "Кол-во дней", example = "10")
        @NotNull(message = "Количество дней обязательно")
        @Min(value = 1, message = "Минимум 1 день")
        @Max(value = 365, message = "Максимум 365 дней")
        private Integer days;

        @NotNull(message = "Дата начала поездки обязательна")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "uuuu-MM-dd", lenient = OptBoolean.FALSE)
        @FutureOrPresent(message = "Дата начала поездки не может быть в прошлом")
        @Schema(description = "Дата начала", example = "2025-11-25")
        private LocalDate startDate;
    }

    @Data
    @Schema(name = "ApplicantBlock")
    public static class ApplicantBlock {

        @Schema(description = "0=Физ.лицо, 1=Юр.лицо", example = "0")
        @NotNull(message = "Тип заявителя (type) обязателен")
        @Min(0) @Max(1)
        private Integer type;

        @Valid
        @Schema(description = "Данные физ. лица (заполнять если type=0)")

        private PersonData person;

        @Valid
        @Schema(description = "Данные юр. лица (заполнять если type=1)")
        private CompanyData company;

        @Schema(description = "Является ли заявитель одним из туристов?", example = "false")
        private Boolean isApplicantTraveler = false;

        @AssertTrue(message = "Для физ. лица (type=0) блок 'person' обязателен")
        private boolean isPersonRequired() {
            if (type != null && type == 0) {
                return person != null;
            }
            return true;
        }

        @AssertTrue(message = "Для юр. лица (type=1) блок 'company' обязателен")
        private boolean isCompanyRequired() {
            if (type != null && type == 1) {
                return company != null;
            }
            return true;
        }
    }

    @Data
    @Schema(name = "PersonData")
    @ValidResidentContact

    public static class PersonData {
        @NotBlank(message = "Имя обязательно")
        @Size(max = 40, message = "Имя слишком длинное (макс 40 символов)")
        private String firstName;
        @NotBlank(message = "Фамилия обязательна")
        @Size(max = 40, message = "Фамилия слишком длинная (макс 40 символов)")
        private String lastName;
        @Size(max = 40, message = "Отчество слишком длинное (макс 40 символов)")
        private String middleName;

        @NotNull(message = "Дата рождения обязательна")
        @Past(message = "Дата рождения должна быть в прошлом")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "uuuu-MM-dd", lenient = OptBoolean.FALSE)

        private LocalDate birthDate;
        @NotBlank
        @NotBlank(message = "Серия паспорта обязательна")
        @Size(min = 1, max = 10, message = "Серия паспорта должна быть от 1 до 10 символов")
        @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Серия паспорта может содержать только латинские буквы и цифры")
        private String passportSeries;

        @NotBlank(message = "Номер паспорта обязателен")
        @Size(min = 7, max = 10, message = "Номер паспорта должен быть от 7 до 10 символов")
        @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Номер паспорта может содержать только латинские буквы и цифры")
        private String passportNumber;
        @NotNull(message = "Гражданство обязательно")
        @JsonProperty("citizenship_id")
        @JsonAlias({"citizenship", "citizenship_id", "citizenshipId"})
        private Long citizenshipId;
        @NotNull @Min(1) @Max(2)
        private Integer gender;
        @NotNull
        private Integer residentId;
        @Schema(description = "ПИНФЛ (Обязателен для резидентов type 1 или 5)", example = "12345678901234")
        @Pattern(regexp = "^[0-9]{14}$", message = "ПИНФЛ должен состоять из 14 цифр")
        private String pinfl;

        @Schema(description = "Телефон", example = "+998001234567")
        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Некорректный формат телефона")
        @Size(max = 20, message = "Телефон слишком длинный")
        private String phone;

        @Schema(description = "Email (Обязателен для нерезидентов type 2)", example = "qwerty@gmail.com")
        @Email
        @Size(max = 100, message = "Email слишком длинный")
        private String email;
    }

    @Data
    @Schema(name = "CompanyData")
    public static class CompanyData {
        @NotBlank @Pattern(regexp = "^[0-9]{9}$")
        private String inn;
        @NotBlank
        @Size(max = 120, message = "Название компании слишком длинное")
        private String name;
        @NotBlank
        @Size(max = 60, message = "ФИО директора слишком длинное")
        private String director;
        @NotBlank
        @Size(max = 200, message = "Адрес слишком длинный")
        private String address;
        @NotBlank
        @Size(max = 100, message = "Название банка слишком длинное")
        private String bankName;
        @NotBlank @Pattern(regexp = "^[0-9]{20}$")
        private String account;
        @Size(max = 20, message = "Телефон слишком длинный (макс 20 символов)")
        private String phone;
        @Email
        private String email;
        @NotNull
        private Long regionId;
        @NotNull
        private Long districtId;
        @NotNull
        private Long residentId;
    }
}
