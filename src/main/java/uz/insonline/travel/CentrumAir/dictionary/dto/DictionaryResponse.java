package uz.insonline.travel.CentrumAir.dictionary.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Ответ справочника в формате остальных методов Centrum Air. */
@Schema(description = "Dictionary response")
public record DictionaryResponse<T>(

        @Schema(description = "Result code: 0 – success, -1 – error", example = "0")
        int result,

        @Schema(description = "Result message", example = "Success")
        String resultMessage,

        @Schema(description = "Number of returned entries", example = "12")
        int totalElements,

        @Schema(description = "Dictionary entries")
        List<T> content
) {
    public static <T> DictionaryResponse<T> success(List<T> content) {
        return new DictionaryResponse<>(0, "Success", content.size(), content);
    }
}
