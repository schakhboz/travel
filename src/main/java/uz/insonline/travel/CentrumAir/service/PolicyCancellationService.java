package uz.insonline.travel.CentrumAir.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.insonline.travel.CentrumAir.dto.policyCancel.CancelPolicyRequest;
import uz.insonline.travel.CentrumAir.dto.policyCancel.CancelPolicyResponse;

import java.sql.CallableStatement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyCancellationService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public CancelPolicyResponse cancelPolicy(CancelPolicyRequest request) {
        Long contractId = request.contractId();
        Integer reasonNumber = request.reasonNumber();
        String reasonText = request.reasonText();

        String selectSql = """
        SELECT 
            p.TB_ID as policy_id,
            a.USER_ID as user_id,
            a.INS_TYPE as pturi_id,
            o.AKT as akt,
            p.TB_DATE_BEGIN as begin_date,
            p.ERSP_STATUS as ersp_status,
            o.OPLATA as premium
        FROM INS_POLIS p
        JOIN INS_ANKETA a ON a.INS_ID = p.TB_ANKETA
        LEFT JOIN INS_OPLATA o ON o.ANKETA_ID = p.TB_ANKETA
        WHERE p.TB_ANKETA = ?
    """;

        Map<String, Object> resultMap = jdbcTemplate.queryForMap(selectSql, contractId);

        Long policyId = ((Number) resultMap.get("policy_id")).longValue();
        Long userId = ((Number) resultMap.get("user_id")).longValue();

        Object erspStatusObj = resultMap.get("ersp_status");
        Integer erspStatus = null;
        if (erspStatusObj instanceof Number) {
            erspStatus = ((Number) erspStatusObj).intValue();
        }

        Object aktObj = resultMap.get("akt");
        Number akt = null;
        if (aktObj instanceof Number) {
            akt = (Number) aktObj;
        }

        Object premiumObj = resultMap.get("premium");
        Number premium = null;
        if (premiumObj instanceof Number) {
            premium = (Number) premiumObj;
        }

        if (erspStatus != null) {
            return CancelPolicyResponse.error("Policy already annulment");
        }

        Object beginObj = resultMap.get("begin_date");
        LocalDate beginDate = null;
        if (beginObj instanceof java.util.Date) {
            beginDate = ((java.util.Date) beginObj).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
        } else if (beginObj != null) {
            return CancelPolicyResponse.error("Invalid date format for begin_date");
        }

        if (beginDate != null) {
            LocalDate today = LocalDate.now();
            if (beginDate.isBefore(today)) {
                return CancelPolicyResponse.error("Policy start date must not be less than today");
            }
        }

        if (akt != null && akt.longValue() != 0) {
            return CancelPolicyResponse.error("Akt exist, cannot cancel");
        }

        try (CallableStatement cs = jdbcTemplate.getDataSource().getConnection().prepareCall("{call MAKEANNUL(?, ?)}")) {
            cs.setLong(1, policyId);
            cs.registerOutParameter(2, Types.INTEGER);
            cs.execute();
            int makeResult = cs.getInt(2);
            log.info("MAKEANNUL result: {}", makeResult);
        } catch (Exception e) {
            log.error("MAKEANNUL failed", e);
            return CancelPolicyResponse.error("MAKEANNUL failed: " + e.getMessage());
        }

        String reason = reasonText;
        if (reasonNumber != null && reasonNumber != 0) {
               reason = reasonText;
            log.info("Using reason number: {}, text: {}", reasonNumber, reasonText);
        }

        SimpleJdbcCall terminationCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("ERSP_VOLUNTARY_INTEGRATIONS")
                .withFunctionName("POLICY_TERMINATION")
                .declareParameters(
                        new SqlOutParameter("return", Types.VARCHAR),
                        new SqlParameter("p_policy_id", Types.NUMERIC),
                        new SqlParameter("p_reason", Types.VARCHAR),
                        new SqlParameter("p_termination_date", Types.DATE),
                        new SqlParameter("p_refund_amount", Types.NUMERIC),
                        new SqlParameter("p_user_id", Types.NUMERIC),
                        new SqlOutParameter("p_error_code", Types.NUMERIC),
                        new SqlOutParameter("p_error_message", Types.VARCHAR)
                );

        Map<String, Object> params = new HashMap<>();
        params.put("p_policy_id", policyId);
        params.put("p_reason", reason);
        params.put("p_termination_date", new java.sql.Date(System.currentTimeMillis()));
        params.put("p_refund_amount", premium != null ? premium : 0);
        params.put("p_user_id", userId);

        Map<String, Object> result = terminationCall.execute(params);

        Object errorCodeObj = result.get("p_error_code");
        Integer errorCode = null;
        if (errorCodeObj instanceof Number) {
            errorCode = ((Number) errorCodeObj).intValue();
        }
        String errorMessage = (String) result.get("p_error_message");

        if (errorCode != null && errorCode != 0) {
            log.error("ERSP POLICY_TERMINATION error: {}", errorMessage);
            jdbcTemplate.update("UPDATE INS_POLIS SET ERSP_STATUS = 0, ERSP_REASON = 'Cancellation error' WHERE TB_ID = ?", policyId);
            return CancelPolicyResponse.error("ERSP error: " + errorMessage);
        }

        jdbcTemplate.update(
                "UPDATE INS_POLIS SET ERSP_STATUS = 1, ERSP_REASON = ?, ERSP_MOTION_USER = ? WHERE TB_ID = ?",
                reason, userId, policyId
        );

        log.info("Policy {} successfully cancelled", policyId);
        return CancelPolicyResponse.success();
    }
}
