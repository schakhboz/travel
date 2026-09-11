package uz.insonline.travel.CentrumAir.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import uz.insonline.travel.CentrumAir.error.ApiErrorResponse;
import uz.insonline.travel.CentrumAir.service.PolicyCancellationService;
import uz.insonline.travel.CentrumAir.service.PolicyJournalFilter;
import uz.insonline.travel.CentrumAir.service.PolicyService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Centrum Air", description = "Endpoints for issue, cancel and get insurance policies")
public class PolicyController {

    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private final PolicyService policyService;
    private final PolicyCancellationService cancellationService;

    @PostMapping("/policy/issue")
    @Operation(summary = "Issue insurance policies",
            description = "Creates one or more policies for a booking. The request is idempotent: repeating it with "
                    + "the same Idempotency-Key returns the result of the first issue and never creates duplicates. "
                    + "After a partial failure a repeat issues only the missing policies. The Idempotency-Key "
                    + "header is required.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Policies issued successfully",
                    content = @Content(schema = @Schema(implementation = PolicyIssueResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "An issue with the same key is already in progress",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Idempotency key reused with a different payload",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PolicyIssueResponse> issuePolicy(
            @RequestBody PolicyIssueRequest request,
            @Parameter(description = "Idempotency key of the issue request, required (ТЗ п. 7.3)",
                    required = true)
            @RequestHeader(value = IDEMPOTENCY_KEY_HEADER, required = false) String idempotencyKey) {
        return ResponseEntity.ok(policyService.issuePolicy(request, idempotencyKey));
    }

    @DeleteMapping("/policy/cancel")
    @Operation(summary = "Cancel a policy", description = "Cancels an existing policy by contract ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Policy cancelled, or cancellation rejected by business rules",
                    content = @Content(schema = @Schema(implementation = CancelPolicyResponse.class))),
            @ApiResponse(responseCode = "404", description = "Policy not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CancelPolicyResponse> cancelPolicy(@RequestBody CancelPolicyRequest request) {
        return ResponseEntity.ok(cancellationService.cancelPolicy(request));
    }

    @GetMapping({"/policies", "/policy/policies"})
    @Operation(summary = "Policy journal",
            description = "Policies of the current partner for the given period, optionally filtered by product "
                    + "and status. Used for monthly reconciliation (ТЗ п. 8.1).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Policies retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PolicyIssueResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid filter (dates expected in yyyy.MM.dd)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PolicyIssueResponse> getPolicies(
            @Parameter(description = "Period start, yyyy.MM.dd", example = "2026.09.01") @RequestParam String dateFrom,
            @Parameter(description = "Period end, yyyy.MM.dd", example = "2026.09.30") @RequestParam String dateTo,
            @Parameter(description = "Product / risk code, e.g. TRAVEL") @RequestParam(required = false) String product,
            @Parameter(description = "Policy status: DRAFT, ISSUED, CANCELLED") @RequestParam(required = false) String status) {
        return ResponseEntity.ok(policyService.getPolicies(PolicyJournalFilter.of(dateFrom, dateTo, product, status)));
    }
}
