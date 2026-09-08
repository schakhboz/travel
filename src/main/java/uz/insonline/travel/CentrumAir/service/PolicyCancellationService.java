package uz.insonline.travel.CentrumAir.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.insonline.travel.CentrumAir.dto.policyCancel.CancelPolicyRequest;
import uz.insonline.travel.CentrumAir.dto.policyCancel.CancelPolicyResponse;
import uz.insonline.travel.CentrumAir.error.CentrumAirApiException;
import uz.insonline.travel.CentrumAir.error.CentrumAirErrorCode;
import uz.insonline.travel.CentrumAir.jdbc.CentrumAirJdbcRepository;
import uz.insonline.travel.CentrumAir.jdbc.ErspGateway;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Аннуляция полиса при возврате билета (ТЗ п. 10.2): до вылета — с возвратом премии 100%,
 * после начала действия полиса аннуляция недоступна.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyCancellationService {

    private static final String POLICY_SQL = """
            SELECT p.TB_ID          AS policy_id,
                   a.USER_ID        AS user_id,
                   o.AKT            AS akt,
                   p.TB_DATE_BEGIN  AS begin_date,
                   p.ERSP_STATUS    AS ersp_status,
                   o.OPLATA         AS premium
            FROM INS_POLIS p
            JOIN INS_ANKETA a ON a.INS_ID = p.TB_ANKETA
            LEFT JOIN INS_OPLATA o ON o.ANKETA_ID = p.TB_ANKETA
            WHERE p.TB_ANKETA = ?
            """;

    private final JdbcTemplate jdbcTemplate;
    private final CentrumAirJdbcRepository jdbcRepository;
    private final ErspGateway erspGateway;

    @Transactional
    public CancelPolicyResponse cancelPolicy(CancelPolicyRequest request) {
        PolicyToCancel policy = findPolicy(request.contractId());

        String rejection = validateCancellable(policy);
        if (rejection != null) {
            log.info("Cancellation of contract {} rejected: {}", request.contractId(), rejection);
            return CancelPolicyResponse.error(rejection);
        }

        log.info("MAKEANNUL result for policy {}: {}", policy.policyId(), jdbcRepository.makeAnnul(policy.policyId()));

        String reason = request.reasonText();
        ErspGateway.ErspResult result = erspGateway.terminate(
                policy.policyId(), reason, LocalDate.now(), policy.premiumOrZero(), policy.userId());

        if (result.failed()) {
            log.error("ERSP termination failed for policy {}: {}", policy.policyId(), result.errorMessage());
            jdbcRepository.markCancellationFailed(policy.policyId(), "Cancellation error");
            return CancelPolicyResponse.error("ERSP error: " + result.errorMessage());
        }

        jdbcRepository.markPolicyCancelled(policy.policyId(), reason, policy.userId());
        log.info("Policy {} successfully cancelled", policy.policyId());
        return CancelPolicyResponse.success();
    }

    /** @return причина отказа на английском либо null, если полис можно аннулировать. */
    private String validateCancellable(PolicyToCancel policy) {
        if (policy.erspStatus() != null) {
            return "Policy already annulment";
        }
        if (policy.beginDate() != null && policy.beginDate().isBefore(LocalDate.now())) {
            return "Policy start date must not be less than today";
        }
        if (policy.akt() != null && policy.akt().signum() != 0) {
            return "Akt exist, cannot cancel";
        }
        return null;
    }

    private PolicyToCancel findPolicy(Long contractId) {
        List<PolicyToCancel> policies = jdbcTemplate.query(POLICY_SQL, POLICY_ROW_MAPPER, contractId);
        if (policies.isEmpty()) {
            throw new CentrumAirApiException(CentrumAirErrorCode.POLICY_NOT_FOUND,
                    "Policy for contract " + contractId + " not found");
        }
        return policies.get(0);
    }

    private static final RowMapper<PolicyToCancel> POLICY_ROW_MAPPER = (rs, rowNum) -> new PolicyToCancel(
            rs.getLong("policy_id"),
            rs.getLong("user_id"),
            rs.getBigDecimal("akt"),
            toLocalDate(rs, "begin_date"),
            nullableInt(rs, "ersp_status"),
            rs.getBigDecimal("premium")
    );

    private static Integer nullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static LocalDate toLocalDate(ResultSet rs, String column) throws SQLException {
        java.sql.Date date = rs.getDate(column);
        return date == null ? null : date.toLocalDate();
    }

    private record PolicyToCancel(Long policyId, Long userId, BigDecimal akt, LocalDate beginDate,
                                  Integer erspStatus, BigDecimal premium) {

        BigDecimal premiumOrZero() {
            return premium != null ? premium : BigDecimal.ZERO;
        }
    }
}
