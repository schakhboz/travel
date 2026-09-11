package uz.insonline.travel.CentrumAir.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uz.insonline.travel.CentrumAir.dto.IssueContentDto;
import uz.insonline.travel.CentrumAir.dto.PolicyItemDto;
import uz.insonline.travel.CentrumAir.dto.response.PolicyIssueResponse;
import uz.insonline.travel.authentication.entity.UserEntity;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Журнал полисов (ТЗ п. 8.1 GET /policies): выпущенные, изменённые и аннулированные полисы
 * за период, пригодные для ежемесячной сверки.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CentrumInsuranceGetService {

    private static final String JOURNAL_SQL = """
            SELECT b.ID              AS booking_id,
                   b.PNR             AS pnr,
                   b.CREATED_AT      AS issue_date,
                   k.TB_NAME || ' ' || k.TB_SURNAME AS insurant_name,
                   p.TB_ID           AS policy_id,
                   p.TB_SERY         AS policy_series,
                   p.TB_NUMBER       AS policy_number,
                   p.FOND_UID        AS policy_uuid,
                   p.TB_PREMIA       AS premium_amount,
                   p.TB_SUMMA        AS liability_amount,
                   p.TB_STATUS       AS policy_status,
                   p.ERSP_STATUS     AS ersp_status,
                   p.TB_DATE_BEGIN   AS start_date,
                   p.TB_DATE_END     AS end_date,
                   a.INS_ID          AS contract_id,
                   cg.GROUP_ID       AS policy_group,
                   pas.RISK_CODES    AS risk_codes
            FROM INS_CENTRUM_AIR_BOOKINGS b
            JOIN INS_CENTRUM_AIR_PASSENGERS pas ON pas.BOOKING_ID = b.ID
            JOIN INS_ANKETA a ON a.INS_ID = pas.CONTRACT_ID
            JOIN INS_POLIS p ON p.TB_ANKETA = a.INS_ID
            LEFT JOIN INS_CENTRUM_AIR_CONTRACT_GROUP cg ON cg.CONTRACT_ID = a.INS_ID
            LEFT JOIN INS_KONTRAGENT k ON k.TB_ID = a.OWNER
            WHERE b.CREATED_AT >= ?
              AND b.CREATED_AT < ?
              AND a.USER_ID = ?
              AND (? IS NULL OR UPPER(pas.RISK_CODES) LIKE '%' || ? || '%')
            ORDER BY b.ID, p.TB_ID
            """;

    private final JdbcTemplate jdbcTemplate;

    public PolicyIssueResponse getPolicies(PolicyJournalFilter filter) {
        Long userId = currentUserId();
        Timestamp from = Timestamp.valueOf(filter.dateFrom().atStartOfDay());
        Timestamp to = Timestamp.valueOf(filter.dateTo().plusDays(1).atStartOfDay());

        List<JournalRow> rows = jdbcTemplate.query(JOURNAL_SQL, JOURNAL_ROW_MAPPER,
                from, to, userId, filter.product(), filter.product());

        List<IssueContentDto> bookings = groupByBooking(rows, filter);
        log.debug("Journal for user {} from {} to {}: {} bookings", userId, from, to, bookings.size());
        return new PolicyIssueResponse(0, "Success", 0, 20, bookings.size(), 1, bookings);
    }

    private List<IssueContentDto> groupByBooking(List<JournalRow> rows, PolicyJournalFilter filter) {
        Map<Long, List<JournalRow>> byBooking = new LinkedHashMap<>();
        for (JournalRow row : rows) {
            if (filter.status() == null || filter.status() == row.status()) {
                byBooking.computeIfAbsent(row.bookingId(), id -> new ArrayList<>()).add(row);
            }
        }

        List<IssueContentDto> bookings = new ArrayList<>(byBooking.size());
        byBooking.values().forEach(bookingRows -> {
            JournalRow first = bookingRows.get(0);
            BigDecimal totalPremium = bookingRows.stream()
                    .map(JournalRow::premiumAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            bookings.add(new IssueContentDto(
                    first.pnr(),
                    first.issueDate(),
                    first.insurantName(),
                    totalPremium,
                    "UZS",
                    bookingRows.stream().map(JournalRow::toPolicyItem).toList()
            ));
        });
        return bookings;
    }

    private static Long currentUserId() {
        UserEntity user = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getTbId();
    }

    private static final RowMapper<JournalRow> JOURNAL_ROW_MAPPER = (rs, rowNum) -> new JournalRow(
            rs.getLong("booking_id"),
            rs.getString("pnr"),
            toLocalDate(rs.getTimestamp("issue_date")),
            rs.getString("insurant_name"),
            rs.getLong("policy_id"),
            rs.getString("policy_series"),
            rs.getLong("policy_number"),
            rs.getString("policy_uuid"),
            rs.getBigDecimal("premium_amount"),
            rs.getBigDecimal("liability_amount"),
            PolicyStatus.of(nullableInt(rs, "policy_status"), nullableInt(rs, "ersp_status")),
            toLocalDate(rs.getDate("start_date")),
            toLocalDate(rs.getDate("end_date")),
            rs.getLong("contract_id"),
            nullableInt(rs, "policy_group"),
            rs.getString("risk_codes")
    );

    private static Integer nullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static LocalDate toLocalDate(java.util.Date date) {
        return date == null ? null : new Date(date.getTime()).toLocalDate();
    }

    /** Строка журнала: один полис в разрезе брони. */
    private record JournalRow(
            Long bookingId,
            String pnr,
            LocalDate issueDate,
            String insurantName,
            Long policyId,
            String policySeries,
            Long policyNumber,
            String policyUuid,
            BigDecimal premiumAmount,
            BigDecimal liabilityAmount,
            PolicyStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Long contractId,
            Integer policyGroup,
            String riskCodes
    ) {

        PolicyItemDto toPolicyItem() {
            return new PolicyItemDto(
                    policyGroup,
                    contractId,
                    policyId,
                    policySeries,
                    String.valueOf(policyNumber),
                    policyUuid,
                    status.name(),
                    premiumAmount,
                    liabilityAmount,
                    riskCodes == null || riskCodes.isBlank() ? List.of() : Arrays.asList(riskCodes.split(",")),
                    startDate == null ? null : startDate.toString(),
                    endDate == null ? null : endDate.toString(),
                    null
            );
        }
    }
}
