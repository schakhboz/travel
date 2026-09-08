package uz.insonline.travel.UzumTravel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.insonline.travel.UzumTravel.payloads.requests.UzumTravelActivateRequest;
import uz.insonline.travel.UzumTravel.payloads.requests.UzumTravelCalculatorRequest;
import uz.insonline.travel.UzumTravel.payloads.requests.UzumTravelContractRequest;
import uz.insonline.travel.UzumTravel.payloads.requests.UzumTravelPolicyRequest;
import uz.insonline.travel.UzumTravel.payloads.responses.UzumTravelCalculatorResponse;
import uz.insonline.travel.UzumTravel.payloads.responses.UzumTravelContractResponse;
import uz.insonline.travel.UzumTravel.payloads.responses.UzumTravelGetPolicyResponse;
import uz.insonline.travel.UzumTravel.payloads.responses.UzumTravelPolicyResponse;
import uz.insonline.travel.UzumTravel.service.UzumTravelService;
import uz.insonline.travel.UzumTravel.validator.UzumTravelValidator;

import java.sql.SQLException;

@RestController
@RequestMapping("/api/uzum-travel")
@RequiredArgsConstructor

@Tag(name = "Uzum Travel Insurance", description = "Air travel insurance issuance")
public class UzumTravelController {

    private final UzumTravelService uzumTravelService;
    private final UzumTravelValidator uzumTravelValidator;

    @Operation(summary = "Create contract", description = "Creates a contract draft, calculates the premium, but does not activate the policy.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Contract created successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UzumTravelContractResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Validation or DB error")
    })
    @PostMapping("/contract")
    public HttpEntity<?> createContract(@RequestBody @Validated UzumTravelContractRequest request) throws SQLException {

        uzumTravelValidator.validate(request);

        UzumTravelContractResponse response = uzumTravelService.createContract(request);
        return ResponseEntity
                .status(response.getResult() == 0 ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @Operation(summary = "Policy activation", description = "Processes payment and generates a policy in the ERSP system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Policy successfully issued",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UzumTravelPolicyResponse.class))})
    })
    @PostMapping("/activate")
    public HttpEntity<?> activatePolicy(@RequestBody @Validated UzumTravelActivateRequest request) {
        UzumTravelPolicyResponse response = uzumTravelService.activatePolicy(request);
        return ResponseEntity
                .status(response.getResult() == 0 ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @Operation(summary = "Get policy info", description = "Returns full information about the policy.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UzumTravelGetPolicyResponse.class))})
    })
    @PostMapping("/get-policy")
    public HttpEntity<?> getPolicy(@RequestBody @Validated UzumTravelPolicyRequest request) {
        return ResponseEntity.ok(uzumTravelService.getPolicy(request));
    }

    @Operation(summary = "Calculator", description = "Calculates the cost of the policy.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Calculation successful",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UzumTravelCalculatorResponse.class))})
    })
    @PostMapping("/calculator")
    public HttpEntity<?> calculate(@RequestBody @Validated UzumTravelCalculatorRequest request) throws SQLException {
        UzumTravelCalculatorResponse response = uzumTravelService.calculatePremium(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Risk dictionary", description = "Returns a list of available risks and their base prices.")
    @GetMapping("/dictionaries")
    public HttpEntity<?> getRisks() {
        return ResponseEntity.ok(uzumTravelService.getAvailableRisksWithIds());
    }
}