package uz.insonline.travel.CentrumAir.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import uz.insonline.travel.CentrumAir.dto.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Request object for issuing insurance policies")
public record PolicyIssueRequest(
        @Schema(description = "PNR (Passenger Name Record) of the booking", example = "PNR123456", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String pnr,

        @Schema(description = "Date and time when the payment was made", example = "2026-08-25T14:30:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull OffsetDateTime paymentTime,

        @Schema(description = "Sales channel (WEB, Call Center, etc.)", example = "WEB", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String salesChannel,

        @Schema(description = "Total premium amount provided by the airline", example = "500000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull BigDecimal totalPremiumAmount,

        @Schema(description = "Currency of the premium (UZS, EUR, USD)", example = "UZS", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String premiumCurrency,

        @Schema(description = "Route details (segments, type, etc.)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Valid RouteDto route,

        @Schema(description = "Language code for response messages (e.g., RU, EN)", example = "RU", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String language,

        @Schema(description = "List of purchased insurance products", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty @Valid List<ProductDto> products,

        @Schema(description = "Transaction details for each policy group", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty @Valid List<TransactionDto> transactions,

        @Schema(description = "Insurant information", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Valid InsurantDto insurant,

        @Schema(description = "List of passengers to be insured", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty @Valid List<PassengerDto> passengers
) {}