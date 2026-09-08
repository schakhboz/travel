package uz.insonline.travel.CentrumAir.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Route information including type and segments")
public record RouteDto(
        @Schema(description = "Route type: OW (one-way) or RT (round-trip)", example = "RT", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String routeType,

        @Schema(description = "Flag indicating if the route is international", example = "true")
        Boolean isInternational,

        @Schema(description = "Flag indicating if the route is Schengen area", example = "false")
        Boolean isSchengen,

        @Schema(description = "List of flight segments", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty List<SegmentDto> segments
) {}