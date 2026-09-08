package uz.insonline.travel.CentrumAir.dictionary.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uz.insonline.travel.CentrumAir.dictionary.dto.CalculationRequest;
import uz.insonline.travel.CentrumAir.dictionary.dto.CalculationResponse;
import uz.insonline.travel.CentrumAir.dictionary.service.PremiumCalculatorService;
import uz.insonline.travel.CentrumAir.error.ApiErrorResponse;

@RestController
@RequiredArgsConstructor
@Tag(name = "Centrum Air calculator", description = "Premium calculation with a full breakdown")
public class CalculatorController {

    private final PremiumCalculatorService calculatorService;

    @PostMapping("/calculator")
    @Operation(summary = "Calculate the premium of a booking",
            description = "Returns the policies a booking is split into, the tariff rates and multipliers each "
                    + "premium is composed of, and the share of every risk. Nothing is stored and no policy is "
                    + "issued: the arithmetic is the same as in POST /policy/issue.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Premium calculated",
                    content = @Content(schema = @Schema(implementation = CalculationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or product combination",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<CalculationResponse> calculate(@RequestBody CalculationRequest request) {
        return ResponseEntity.ok(calculatorService.calculate(request));
    }
}
