package uz.insonline.travel.CentrumAir.jdbc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;
import uz.insonline.travel.CentrumAir.config.CentrumAirProperties;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Обмен с НАПП (ЕАИС) через пакет ERSP_VOLUNTARY_INTEGRATIONS: регистрация полиса,
 * подтверждение оплаты и расторжение.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ErspGateway {

    private final JdbcTemplate jdbcTemplate;
    private final CentrumAirProperties properties;

    /** Регистрация полиса в НАПП и подтверждение оплаты (ТЗ п. 7.1, шаг 3). */
    public void issueAndConfirm(Long userId, Long contractId, Long policyId) {
        call(properties.getErsp().getIssueFunction(), "req_id", userId, contractId, policyId);
        call(properties.getErsp().getConfirmFunction(), "p_req_id", userId, contractId, policyId);
    }

    /** Расторжение полиса в НАПП (ТЗ п. 10.2). */
    public ErspResult terminate(Long policyId, String reason, LocalDate terminationDate,
                                BigDecimal refundAmount, Long userId) {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName(properties.getErsp().getCatalog())
                .withFunctionName(properties.getErsp().getTerminationFunction())
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
        params.put("p_termination_date", java.sql.Date.valueOf(terminationDate));
        params.put("p_refund_amount", refundAmount);
        params.put("p_user_id", userId);

        return ErspResult.from(call.execute(params));
    }

    private void call(String functionName, String requestParamName, Long userId, Long contractId, Long policyId) {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName(properties.getErsp().getCatalog())
                .withFunctionName(functionName)
                .declareParameters(
                        new SqlOutParameter("return", Types.VARCHAR),
                        new SqlParameter("p_user_id", Types.NUMERIC),
                        new SqlParameter("p_contract_id", Types.NUMERIC),
                        new SqlParameter("p_policy_id", Types.NUMERIC),
                        new SqlParameter(requestParamName, Types.NUMERIC),
                        new SqlOutParameter("p_error_code", Types.NUMERIC),
                        new SqlOutParameter("p_error_message", Types.VARCHAR)
                );

        Map<String, Object> params = new HashMap<>();
        params.put("p_user_id", userId);
        params.put("p_contract_id", contractId);
        params.put("p_policy_id", policyId);
        params.put(requestParamName, 0L);

        ErspResult result = ErspResult.from(call.execute(params));
        if (result.failed()) {
            throw new IllegalStateException("ERSP " + functionName + " error: " + result.errorMessage());
        }
        log.debug("ERSP {} succeeded for contract {} policy {}", functionName, contractId, policyId);
    }

    public record ErspResult(int errorCode, String errorMessage) {

        static ErspResult from(Map<String, Object> result) {
            Object code = result.get("p_error_code");
            return new ErspResult(
                    code instanceof Number number ? number.intValue() : 0,
                    (String) result.get("p_error_message")
            );
        }

        public boolean failed() {
            return errorCode != 0;
        }
    }
}
