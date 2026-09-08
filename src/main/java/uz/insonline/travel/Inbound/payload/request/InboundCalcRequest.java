package uz.insonline.travel.Inbound.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
@Schema(name = "InboundCalcRequest", description = "Запрос на расчет стоимости")
public class InboundCalcRequest {
    @NotNull(message = "programId не может быть пустым")
    @Min(value = 1, message = "programId должен быть больше 0")
    @Max(value = 2, message = "programId не должен быть больше 2")
    @Schema(description = "ID Программы (1=Standart, 2=Comfort)", example = "1")
    private Long programId;

    @NotNull(message = "days (количество дней) обязательно")
    @Min(value = 1, message = "Количество дней должно быть минимум 1")
    @Max(value = 365, message = "Максимальное количество дней — 365")
    @Schema(description = "Количество дней", example = "10")
    private Integer days;

    @NotNull(message = "travelersCount (количество туристов) обязательно")
    @Min(value = 1, message = "Должен быть хотя бы 1 турист")
    @Max(value = 50, message = "Максимальное количество туристов в одном расчете — 50")
    @Schema(description = "Количество путешественников", example = "2")
    private Integer travelersCount;
}