package uz.insonline.travel.CentrumAir.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErspPageResponse<T> {

    private Integer result;
    private String resultMessage;
    private Integer page;
    private Integer size;
    private Integer totalElements;
    private Integer totalPages;
    private List<T> content;
}
