package uz.insonline.travel.CentrumAir.dictionary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import uz.insonline.travel.CentrumAir.dto.ProductDto;

import java.time.LocalDate;
import java.util.List;

/**
 * Запрос калькулятора: параметры поездки без персональных данных.
 * Даты нужны только для ВЗР — тариф по нему суточный.
 */
@Schema(description = "Premium calculation request")
public record CalculationRequest(

        @Schema(description = "Route type: OW (one-way) or RT (round-trip)", example = "RT",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String routeType,

        @Schema(description = "International route flag", example = "true")
        Boolean isInternational,

        @Schema(description = "Schengen route flag: adds 15 days to the travel coverage period", example = "false")
        Boolean isSchengen,

        @Schema(description = "Number of insured passengers", example = "2",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer passengerCount,

        @Schema(description = "Date of the first departure. Required for TRAVEL", example = "2026-09-04")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate departureDate,

        @Schema(description = "Departure date of the last segment (the return flight). Required for TRAVEL",
                example = "2026-09-15")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate returnDate,

        @Schema(description = "Arrival date of the last segment. Used for Schengen routes; "
                + "defaults to returnDate", example = "2026-09-15")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate arrivalDate,

        @Schema(description = "Purchased products with quantities", requiredMode = Schema.RequiredMode.REQUIRED)
        List<ProductDto> products
) {
}
