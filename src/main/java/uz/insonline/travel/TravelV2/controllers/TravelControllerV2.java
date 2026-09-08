package uz.insonline.travel.TravelV2.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import uz.insonline.travel.TravelV2.payloads.requests.PolicyRequestV2;
import uz.insonline.travel.TravelV2.payloads.responses.PolicyCreateResponse;
import uz.insonline.travel.TravelV2.services.TravelServiceV2;
import uz.insonline.travel.commons.payload.response.ApiResponseAll;
import uz.insonline.travel.TravelV2.payloads.responses.ContractResponse;
import uz.insonline.travel.TravelV2.payloads.requests.ContractRequestV2;
import uz.insonline.travel.TravelV2.payloads.responses.GetPolicyResponse;
import uz.insonline.travel.TravelV2.payloads.requests.PolicyCreateRequest;
import uz.insonline.travel.TravelV2.payloads.requests.PolicyAnnulmentRequest;
import uz.insonline.travel.TravelV2.payloads.responses.PolicyAnnulmentResponse;

import java.sql.SQLException;

@RestController
@RequestMapping("/api/v2/travel")
@RequiredArgsConstructor
@Tag(name = "TravelV2", description = "APIs for Travel V2 product")
public class TravelControllerV2 {
    private final TravelServiceV2 service;

    @Operation(summary = "API to create Travel", description = "Creating Travel contract", operationId = "createContractV1")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "completed successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ContractResponse.class))})})
    @PostMapping("/contract")
    public HttpEntity<?> create(
            @RequestBody @Validated ContractRequestV2 dto) throws SQLException {
        ApiResponseAll response = service.create(dto);
        return ResponseEntity
                .status(response.getResult() == 0 ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @Operation(summary = "API request for processing payment without merchandise.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "completed successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = PolicyCreateResponse.class))})
    })
    @PutMapping("/payment")
    public HttpEntity<?> payment(@Valid @RequestBody PolicyCreateRequest dto) {
        return ResponseEntity.ok(service.payment(dto));
    }

    @Operation(summary = "API request for get full info about policy.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "completed successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = GetPolicyResponse.class))})
    })
    @PostMapping("/get-policy")
    public HttpEntity<?> getPolicy(@Valid @RequestBody PolicyRequestV2 dto) {
        return ResponseEntity.ok(service.getPolicy(dto));
    }

    @Operation(summary = "API to annulment policy")
    @DeleteMapping("/annulment-policy")
    public ResponseEntity<?> annulmentPolicy(@RequestBody @Valid PolicyAnnulmentRequest dto) {
        PolicyAnnulmentResponse response = service.policyAnnulment(dto);
        return ResponseEntity
                .status(response.getResult() == 0 ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

}
