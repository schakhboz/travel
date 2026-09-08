package uz.insonline.travel.CentrumAir;

import uz.insonline.travel.CentrumAir.dto.*;
import uz.insonline.travel.CentrumAir.dto.request.PolicyIssueRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/** Тестовые данные запроса на выпуск. */
final class PolicyRequests {

    private static final OffsetDateTime PAYMENT = OffsetDateTime.of(2026, 9, 1, 10, 0, 0, 0, ZoneOffset.UTC);

    private PolicyRequests() {
    }

    static PolicyIssueRequest issueRequest(String pnr, List<ProductDto> products) {
        return new PolicyIssueRequest(
                pnr,
                PAYMENT,
                "WEB",
                new BigDecimal("500000"),
                "UZS",
                route(),
                "RU",
                products,
                List.of(new TransactionDto(1, "tx-1")),
                insurant(),
                List.of(passenger()),
                null
        );
    }

    static RouteDto route() {
        return new RouteDto("RT", true, false, List.of(
                new SegmentDto(1, "C6-101", "TAS", "DXB",
                        PAYMENT.plusDays(3), PAYMENT.plusDays(3).plusHours(3)),
                new SegmentDto(2, "C6-102", "DXB", "TAS",
                        PAYMENT.plusDays(10), PAYMENT.plusDays(10).plusHours(3))
        ));
    }

    static InsurantDto insurant() {
        return new InsurantDto("998901111111", "insurant@example.com", "Ташкент", 1,
                "32151214125142", "AB", "1111111", "Ivan", "Ivanov", "Ivanovich",
                LocalDate.of(1995, 8, 26), 1, 0);
    }

    static PassengerDto passenger() {
        return new PassengerDto("AB", "1111111", "Ivan", "Ivanov", "Ivanovich", 1,
                LocalDate.of(1995, 8, 26), 1, "32151214125142",
                "passenger@example.com", "998901111111", 0);
    }
}
