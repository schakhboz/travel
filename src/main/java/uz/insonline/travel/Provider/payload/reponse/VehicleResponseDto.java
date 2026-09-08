package uz.insonline.travel.Provider.payload.reponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uz.insonline.travel.commons.payload.response.ApiResponseAll;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponseDto extends ApiResponseAll {

    @Schema(description = "Дата выдачи тех. паспорта")
    private String tech_passport_issue_date;

    @Schema(description = "marka id")
    private String marka_id;

    @Schema(description = "model id")
    private String model_id;

    @Schema(description = "Тип авто")
    private String vehicle_type_id;

    @Schema(description = "Модель транспортного средства")
    private String model_name;

    @Schema(description = "vehicle color")
    private String vehicle_color;

    @Schema(description = "Год выпуска авто")
    private String issue_year;

    @Schema(description = "Номер кузова")
    private String body_number;

    @Schema(description = "Номер двигателя")
    private String engine_number;

    @Schema(description = "владельцем транспорта является (fy=0 физическое, fy=1 юридическое) лицо ")
    private String fy;

    @Schema(description = "имя владельца")
    private String orgname;

    @Schema(description = "last_name")
    private String last_name;

    @Schema(description = "first_name")
    private String first_name;

    @Schema(description = "middle_name")
    private String middle_name;

    @Schema(description = "Инн")
    private String inn;

    @Schema(description = "Пинфл")
    private String pinfl;
}
