package uz.insonline.travel.CentrumAir.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uz.insonline.travel.CentrumAir.dto.IssueContentDto;
import uz.insonline.travel.CentrumAir.dto.PolicyItemDto;
import uz.insonline.travel.CentrumAir.dto.response.PolicyIssueResponse;
import uz.insonline.travel.authentication.entity.UserEntity;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CentrumInsuranceGetService {

    private final JdbcTemplate jdbcTemplate;

    public PolicyIssueResponse getPolicies(String dateFrom, String dateTo) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        Long userId = user.getTbId();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        LocalDate fromDate = LocalDate.parse(dateFrom, formatter);
        LocalDate toDate = LocalDate.parse(dateTo, formatter);

        Timestamp fromTs = Timestamp.valueOf(fromDate.atStartOfDay());
        Timestamp toTs = Timestamp.valueOf(toDate.plusDays(1).atStartOfDay());

        String sql = """
        SELECT 
            b.ID as booking_id,
            b.PNR,
            b.CREATED_AT as issue_date,
            k.TB_NAME || ' ' || k.TB_SURNAME as insurant_name,
            p.TB_ID as policy_id,
            p.TB_SERY as policy_series,
            p.TB_NUMBER as policy_number,
            p.TB_PREMIA as premium_amount,
            p.TB_SUMMA as liability_amount,
            p.TB_STATUS as status,
            p.TB_DATE_BEGIN as start_date,
            p.TB_DATE_END as end_date,
            a.INS_ID as contract_id,
            cg.GROUP_ID as policy_group,
            pas.RISK_CODES as risk_codes
        FROM INS_CENTRUM_AIR_BOOKINGS b
        JOIN INS_CENTRUM_AIR_PASSENGERS pas ON pas.BOOKING_ID = b.ID
        JOIN INS_ANKETA a ON a.INS_ID = pas.CONTRACT_ID
        JOIN INS_POLIS p ON p.TB_ANKETA = a.INS_ID
        LEFT JOIN INS_CENTRUM_AIR_CONTRACT_GROUP cg ON cg.CONTRACT_ID = a.INS_ID
        LEFT JOIN INS_KONTRAGENT k ON k.TB_ID = a.OWNER
        WHERE b.CREATED_AT >= ? AND b.CREATED_AT < ?
          AND a.USER_ID = ?
        ORDER BY b.ID, p.TB_ID
    """;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, fromTs, toTs, userId);

        Map<Long, List<Map<String, Object>>> bookingMap = rows.stream()
                .collect(Collectors.groupingBy(row -> ((Number) row.get("booking_id")).longValue()));

        List<IssueContentDto> contentList = new ArrayList<>();

        for (Map.Entry<Long, List<Map<String, Object>>> entry : bookingMap.entrySet()) {
            List<Map<String, Object>> bookingRows = entry.getValue();
            Map<String, Object> firstRow = bookingRows.get(0);

            String pnr = (String) firstRow.get("PNR");

            Object issueObj = firstRow.get("issue_date");
            LocalDate issueDate = null;
            if (issueObj instanceof java.util.Date) {
                issueDate = ((java.util.Date) issueObj).toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate();
            } else {
                issueDate = LocalDate.now();
            }

            String insurantName = (String) firstRow.get("insurant_name");

            BigDecimal totalPremium = bookingRows.stream()
                    .map(r -> (BigDecimal) r.get("premium_amount"))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<PolicyItemDto> policies = new ArrayList<>();
            for (Map<String, Object> row : bookingRows) {
                Long policyId = ((Number) row.get("policy_id")).longValue();
                String policySeries = (String) row.get("policy_series");
                Long policyNumber = ((Number) row.get("policy_number")).longValue();
                BigDecimal premiumAmount = (BigDecimal) row.get("premium_amount");
                BigDecimal liabilityAmount = (BigDecimal) row.get("liability_amount");

                Object statusObj = row.get("status");
                Integer status = null;
                if (statusObj instanceof Number) {
                    status = ((Number) statusObj).intValue();
                }

                LocalDate startDate = null;
                Object startObj = row.get("start_date");
                if (startObj instanceof java.util.Date) {
                    startDate = ((java.util.Date) startObj).toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate();
                } else {
                    startDate = LocalDate.now();
                }

                LocalDate endDate = null;
                Object endObj = row.get("end_date");
                if (endObj instanceof java.util.Date) {
                    endDate = ((java.util.Date) endObj).toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate();
                } else {
                    endDate = LocalDate.now();
                }

                Long contractId = ((Number) row.get("contract_id")).longValue();

                Object pgObj = row.get("policy_group");
                Integer policyGroup = null;
                if (pgObj instanceof Number) {
                    policyGroup = ((Number) pgObj).intValue();
                }

                String riskCodesStr = (String) row.get("risk_codes");
                List<String> riskCodes = (riskCodesStr != null && !riskCodesStr.isEmpty())
                        ? Arrays.asList(riskCodesStr.split(","))
                        : Collections.emptyList();

                PolicyItemDto policyItem = new PolicyItemDto(
                        policyGroup,
                        contractId,
                        policyId,
                        policySeries,
                        String.valueOf(policyNumber),
                        String.valueOf(policyNumber),
                        status != null ? String.valueOf(status) : "UNKNOWN",
                        premiumAmount,
                        liabilityAmount,
                        riskCodes,
                        startDate.toString(),
                        endDate.toString(),
                        null
                );
                policies.add(policyItem);
            }

            IssueContentDto content = new IssueContentDto(
                    pnr,
                    issueDate,
                    insurantName,
                    totalPremium,
                    "UZS",
                    policies
            );
            contentList.add(content);
        }

        long totalElements = contentList.size();
        return new PolicyIssueResponse(0, "Success", 0, 20, totalElements, 1, contentList);
    }

}
