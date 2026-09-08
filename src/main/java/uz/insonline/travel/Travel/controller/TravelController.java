package uz.insonline.travel.Travel.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.insonline.travel.Travel.payload.request.PolicyCreateRequest;
import uz.insonline.travel.Travel.payload.request.main.ContractRequest;
import uz.insonline.travel.Travel.payload.request.main.TravelCalculatorRequest;
import uz.insonline.travel.Travel.payload.response.CalculatorResponse;
import uz.insonline.travel.Travel.payload.response.ContractResponse;
import uz.insonline.travel.Travel.payload.response.FileResponse;
import uz.insonline.travel.Travel.payload.response.PolicyCreateResponse;
import uz.insonline.travel.Travel.payload.response.reference.*;
import uz.insonline.travel.Travel.service.TravelReferenceService;
import uz.insonline.travel.Travel.service.TravelService;
import uz.insonline.travel.commons.config.ResponseFileConfig;
import uz.insonline.travel.commons.payload.response.ErrorResponse;

import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/travel")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Travel", description = "APIs for Travel product")
public class TravelController {

    TravelService service;
    ResponseFileConfig responseFileConfig;
    TravelReferenceService referenceService;

    @Operation(summary = "API to calculate Travel (cost) premia")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "completed successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CalculatorResponse.class))})})
    @PostMapping("/calculator")
    public HttpEntity<?> calculate(@RequestBody @Validated TravelCalculatorRequest dto) {
        CalculatorResponse calculatorResponse = service.calculator(dto);

        if (calculatorResponse.getResult() != 0)
            return new ResponseEntity<>(calculatorResponse, HttpStatus.BAD_REQUEST);
        return ResponseEntity.ok(calculatorResponse);
    }

    @Operation(summary = "API to create Travel", description = "Creating Travel contract", operationId = "createContractV1")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "completed successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ContractResponse.class))})})
    @PostMapping("/contract")
    public HttpEntity<?> create(
            @RequestBody @Validated ContractRequest dto) throws SQLException {
        return new ResponseEntity<>(service.create(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "API to get travel types", description = "List of travel types (2)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "completed successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = GetTravelTypesResponse.class))})})
    @Cacheable(value = "travel-types", key = "#language")
    @GetMapping("/types")
    public HttpEntity<?> getTravelTypes(@RequestHeader(value = "Accept-Language", required = false, defaultValue = "ru") String language) {
        try {
            return new ResponseEntity<>(referenceService.getTravelTypes(language), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "API to get travel groups", description = "List of travel groups")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "completed successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = GetTravelGroupsResponse.class))})})
    @Cacheable(value = "travel-groups", key = "#activityTypeId + '-' + #language")
    @GetMapping("/groups")
    public HttpEntity<?> getTravelGroups(@RequestParam(value = "activityTypeId", required = false) Integer activityTypeId,
                                         @RequestHeader(value = "Accept-Language", required = false, defaultValue = "ru") String language) {
        return new ResponseEntity<>(referenceService.getTravelGroups(activityTypeId, language), HttpStatus.OK);
    }

    @Operation(summary = "API to get travel activities", description = "List of travel activities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "completed successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = GetTravelActivitiesResponse.class))})})
    @Cacheable(value = "travel-activities", key = "#language")
    @GetMapping("/activities")
    public HttpEntity<?> getTravelActivities(@RequestHeader(value = "Accept-Language", required = false, defaultValue = "ru") String language) {
        return new ResponseEntity<>(referenceService.getTravelActivities(language), HttpStatus.OK);
    }

    @Operation(summary = "API to get travel countries", description = "List of travel countries")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "completed successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = GetTravelCountriesResponse.class))})})
    @Cacheable(value = "travel-countries", key = "#language")
    @GetMapping("/countries")
    public HttpEntity<?> getTravelCountries(@RequestHeader(value = "Accept-Language", required = false, defaultValue = "ru") String language) {
        return new ResponseEntity<>(referenceService.getTravelCountries(language), HttpStatus.OK);
    }

    @Operation(summary = "API to get travel multi day types", description = "List of travel multi day types")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "completed successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = GetTravelMultiDayTypesResponse.class))})})
    @Cacheable(value = "travel-multi-day-types", key = "#travelTypeId + '-' + #language")
    @GetMapping("/multi-day-types")
    public HttpEntity<?> getTravelMultiDayTypes(@RequestParam(value = "travelTypeId", required = false) Integer travelTypeId,
                                                @RequestHeader(value = "Accept-Language", required = false, defaultValue = "ru") String language) {
        return new ResponseEntity<>(referenceService.getTravelMultiDayTypes(travelTypeId, language), HttpStatus.OK);
    }

    @Operation(summary = "API to get travel program types", description = "List of travel programs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "completed successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = GetTravelProgramsResponse.class))})})
    @Cacheable(value = "travel-programs", key = "#countries", condition = "#countries != null")
    @GetMapping("/programs")
    public HttpEntity<?> getTravelPrograms(@RequestParam(value = "countries") String countries) {
        if (countries == null || countries.trim().isEmpty()) {
            throw new IllegalArgumentException("countries query parameter cannot be empty or blank");
        }
        List<Integer> countriesList;
        try {
            countriesList = Stream.of(countries.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .toList();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("countries query parameter must be a comma-separated list of numeric IDs");
        }
        return new ResponseEntity<>(referenceService.getTravelPrograms(countriesList), HttpStatus.OK);
    }

    @Operation(summary = "API to get travel program types for site", description = "List of travel programs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "completed successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = GetTravelProgramsResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "403", description = "Access denied for this user")
    })
    @GetMapping("/programs/for-site")
    public HttpEntity<?> getTravelProgramsV2(@RequestParam Integer activityId,
                                             @RequestParam(value = "countries", required = false) String countries) {
        try {
            return ResponseEntity.ok(referenceService.getTravelProgramsV2(countries, activityId));
        } catch (AccessDeniedException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage(), -1));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse(e.getMessage(), -1));
        }
    }

    @Operation(summary = "API request for processing payment without merchandise.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "completed successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = PolicyCreateResponse.class))})})
    @PutMapping("/payment")
    public HttpEntity<?> payment(@Valid @RequestBody PolicyCreateRequest dto) {
        PolicyCreateResponse payment = service.payment(dto);
        return ResponseEntity.ok(payment);
    }

    @Operation(summary = "API to get travel program types", description = "List of travel programs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "completed successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = PolicyCreateResponse.class))})})
    @GetMapping("/get-policy-link")
    public HttpEntity<?> getPolicyLink(@RequestParam(value = "contract_id") Long countryId) {
        PolicyCreateResponse policyLink = service.getPolicyLink(countryId);
        return new ResponseEntity<>(policyLink, HttpStatus.OK);
    }

    @GetMapping(value = "/policy")
    @Operation(summary = "API to obtain insurance policy in pdf format.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "completed successfully",
                    content = {@Content(mediaType = "application/pdf")})})
    public ResponseEntity<?> getPolicy(@RequestParam(name = "key") String hashKey) {
        FileResponse fileResponse = service.getPolicy(hashKey);
        if (fileResponse.isError()) {
            throw new NoSuchElementException("Policy not found");
        }
        return responseFileConfig.getResponseEntity(fileResponse.bytes(), fileResponse.fileName());
    }

    @GetMapping(value = "/policy-old/{policyId}/{contractId}")
    @Operation(summary = "API to obtain insurance policy old version in pdf format.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "completed successfully",
                    content = {@Content(mediaType = "application/pdf")})})
    public ResponseEntity<?> getOldPolicy(@PathVariable Long policyId, @PathVariable Long contractId) {
        FileResponse fileResponse = service.getOldPolicy(policyId, contractId);
        if (fileResponse.isError()) {
            throw new NoSuchElementException("Policy not found");
        }
        return responseFileConfig.getResponseEntity(fileResponse.bytes(), fileResponse.fileName());
    }

}

