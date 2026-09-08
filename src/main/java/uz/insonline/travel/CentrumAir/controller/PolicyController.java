package uz.insonline.travel.CentrumAir.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.insonline.travel.CentrumAir.dto.policyCancel.CancelPolicyRequest;
import uz.insonline.travel.CentrumAir.dto.policyCancel.CancelPolicyResponse;
import uz.insonline.travel.CentrumAir.dto.request.PolicyIssueRequest;
import uz.insonline.travel.CentrumAir.dto.response.PolicyIssueResponse;
import uz.insonline.travel.CentrumAir.service.PolicyCancellationService;
import uz.insonline.travel.CentrumAir.service.PolicyService;

@RestController
@RequestMapping("/policy")
@RequiredArgsConstructor
@Tag(name = "Centrum Air", description = "Endpoints for issue, cancel and get insurance policies")
public class PolicyController {

    private final PolicyService policyService;
    private final PolicyCancellationService cancellationService;

    @DeleteMapping("/cancel")
    @Operation(summary = "Cancel a policy", description = "Cancels an existing policy by contract ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Policy cancelled successfully",
                    content = @Content(schema = @Schema(implementation = CancelPolicyResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or policy cannot be cancelled"),
            @ApiResponse(responseCode = "404", description = "Policy not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CancelPolicyResponse> cancelPolicy(@RequestBody CancelPolicyRequest request) {
        CancelPolicyResponse response = cancellationService.cancelPolicy(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/issue")
    @Operation(summary = "Issue insurance policies", description = "Creates one or more policies.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Policies issued successfully",
                    content = @Content(schema = @Schema(implementation = PolicyIssueResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PolicyIssueResponse> issuePolicy(@RequestBody PolicyIssueRequest request) {
        PolicyIssueResponse response = policyService.issuePolicy(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/policies")
    @Operation(summary = "Get policies by date range", description = "Retrieves all policies issued for the current user within the specified date range (based on booking creation date).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Policies retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PolicyIssueResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid date format (expected yyyy.MM.dd)"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PolicyIssueResponse> getPolicies(
            @RequestParam String dateFrom,
            @RequestParam String dateTo) {
        PolicyIssueResponse response = policyService.getPolicies(dateFrom, dateTo);
        return ResponseEntity.ok(response);
    }
}