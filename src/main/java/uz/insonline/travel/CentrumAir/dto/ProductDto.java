package uz.insonline.travel.CentrumAir.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Insurance product code and quantity")
public record ProductDto(
        @Schema(description = "Product code (STANDARD, EXTENDED, MAXIMUM, TRAVEL, ADDON_BAGGAGE, ANIMAL)", example = "STANDARD", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String productCode,

        @Schema(description = "Quantity (number of baggage items or animals)", example = "2")
        Integer quantity
) {}