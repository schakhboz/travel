package uz.insonline.travel.CentrumAir.service;

import uz.insonline.travel.CentrumAir.dto.response.ErspResponse;

import java.util.List;

/** Результат выпуска заявки: бронь и выпущенные по ней полисы. */
public record IssueResult(Long bookingId, List<ErspResponse> policies) {
}
