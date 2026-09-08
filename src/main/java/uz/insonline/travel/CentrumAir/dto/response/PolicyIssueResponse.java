package uz.insonline.travel.CentrumAir.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.insonline.travel.CentrumAir.dto.IssueContentDto;

import java.util.List;

@Schema(description = "Response containing issued policies grouped by booking")
public record PolicyIssueResponse(
        @Schema(description = "Result code: 0 – success, -1 – error", example = "0")
        int result,

        @Schema(description = "Result message", example = "Success")
        String resultMessage,

        @Schema(description = "Page number (0-based)", example = "0")
        int page,

        @Schema(description = "Page size", example = "20")
        int size,

        @Schema(description = "Total number of elements", example = "1")
        long totalElements,

        @Schema(description = "Total number of pages", example = "1")
        int totalPages,

        @Schema(description = "List of bookings with their policies")
        List<IssueContentDto> content
) {
    public static PolicyIssueResponse success(List<IssueContentDto> content) {
        return new PolicyIssueResponse(0, "Success", 0, 20, content.size(), 1, content);
    }
}
