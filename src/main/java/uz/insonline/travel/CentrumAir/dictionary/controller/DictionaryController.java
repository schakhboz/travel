package uz.insonline.travel.CentrumAir.dictionary.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.insonline.travel.CentrumAir.dictionary.dto.DictionaryResponse;
import uz.insonline.travel.CentrumAir.dictionary.dto.RiskDto;
import uz.insonline.travel.CentrumAir.dictionary.dto.TariffDto;
import uz.insonline.travel.CentrumAir.dictionary.service.DictionaryService;
import uz.insonline.travel.CentrumAir.error.ApiErrorResponse;

@RestController
@RequestMapping("/dictionary")
@RequiredArgsConstructor
@Tag(name = "Centrum Air dictionaries",
        description = "Tariff matrix and risk registry the premium calculation is based on")
public class DictionaryController {

    private final DictionaryService dictionaryService;

    @GetMapping("/tariffs")
    @Operation(summary = "Tariff matrix",
            description = "Gross rates per product and route type, grouped by accounting group (ТЗ п. 5.4).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tariffs retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DictionaryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid filter",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<DictionaryResponse<TariffDto>> tariffs(
            @Parameter(description = "Accounting group: 0, 1, 2 or 3") @RequestParam(required = false) Integer policyGroup,
            @Parameter(description = "Product code, e.g. MAXIMUM") @RequestParam(required = false) String tariffCode) {
        return ResponseEntity.ok(DictionaryResponse.success(dictionaryService.tariffs(policyGroup, tariffCode)));
    }

    @GetMapping("/risks")
    @Operation(summary = "Risk registry",
            description = "Insured sums and insurance classes of the risks the policies are composed of (ТЗ п. 3.1).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Risks retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DictionaryResponse.class)))
    })
    public ResponseEntity<DictionaryResponse<RiskDto>> risks(
            @Parameter(description = "Risk code, e.g. ACCIDENT") @RequestParam(required = false) String riskCode) {
        return ResponseEntity.ok(DictionaryResponse.success(dictionaryService.risks(riskCode)));
    }
}
