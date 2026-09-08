package uz.insonline.travel.UzumTravel.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.OracleConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.insonline.travel.UzumTravel.payloads.requests.UzumTravelActivateRequest;
import uz.insonline.travel.UzumTravel.payloads.requests.UzumTravelCalculatorRequest;
import uz.insonline.travel.UzumTravel.payloads.requests.UzumTravelContractRequest;
import uz.insonline.travel.UzumTravel.payloads.requests.UzumTravelPolicyRequest;
import uz.insonline.travel.UzumTravel.payloads.responses.UzumTravelCalculatorResponse;
import uz.insonline.travel.UzumTravel.payloads.responses.UzumTravelContractResponse;
import uz.insonline.travel.UzumTravel.payloads.responses.UzumTravelGetPolicyResponse;
import uz.insonline.travel.UzumTravel.payloads.responses.UzumTravelPolicyResponse;
import uz.insonline.travel.authentication.entity.UserEntity;
import uz.insonline.travel.commons.exceptions.PolicyNotFoundException;
import uz.insonline.travel.commons.exceptions.ValidationException;
import uz.insonline.travel.commons.payload.request.InsurantDto;
import uz.insonline.travel.commons.payload.request.InsurantOrganizationDto;
import uz.insonline.travel.commons.payload.request.InsurantPersonDto;
import uz.insonline.travel.commons.payload.request.TravelerDto;
import uz.insonline.travel.commons.util.CustomUtil;
import uz.insonline.travel.commons.util.Utils;
import uz.insonline.travel.log.service.LogService;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UzumTravelService {

    private final JdbcTemplate jdbcTemplate;
    private final LogService logService;

    private static final String BASE_URL = "https://ersp.e-osgo.uz";


    /**
     * Contract creation (Draft)
     */
    @Transactional
    public UzumTravelContractResponse createContract(UzumTravelContractRequest request) throws SQLException {
        UzumTravelContractResponse response;
        UserEntity user = Utils.getUser();

        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("FOR_PASSENGER_INSURANCE")
                .withFunctionName("CREATE_PASSENGER_CONTRACT")
                .declareParameters(
                        new SqlOutParameter("out_error", Types.NUMERIC),
                        new SqlOutParameter("out_error_text", Types.VARCHAR)
                );


        InsurantDto insurant = request.getInsurant();
        // Determining insurant type
        int isJuridical = insurant.getInsurantType();

        InsurantPersonDto person = insurant.getPerson();
        InsurantOrganizationDto org = insurant.getOrganization();

        SqlParameterSource inParams = new MapSqlParameterSource()
                .addValue("p_user_id", user.getTbId())
                .addValue("p_contract_type", "UZUM_TRAVEL")

                .addValue("p_start_date", CustomUtil.toDate(request.getStartDate(), "yyyy-MM-dd"), Types.DATE)
                .addValue("p_end_date", CustomUtil.toDate(request.getEndDate(), "yyyy-MM-dd"), Types.DATE)

                // Passing explicit type
                .addValue("p_fizyur", isJuridical)

                // Individual data (fill only if type == 0)
                .addValue("p_owner_name", (isJuridical == 0 && person != null) ? person.getFirstName() : null)
                .addValue("p_owner_surname", (isJuridical == 0 && person != null) ? person.getLastName() : null)
                .addValue("p_owner_middle", (isJuridical == 0 && person != null) ? person.getMiddleName() : null)
                .addValue("p_owner_birth", (isJuridical == 0 && person != null) ? CustomUtil.toDate(person.getBirthDate(), "yyyy-MM-dd") : null, Types.DATE)
                .addValue("p_owner_pass_sery", (isJuridical == 0 && person != null) ? person.getPassportSeries() : null)
                .addValue("p_owner_pass_num", (isJuridical == 0 && person != null) ? person.getPassportNumber() : null)
                .addValue("p_owner_pinfl", (isJuridical == 0 && person != null) ? person.getPinfl() : null)
                .addValue("p_owner_citiz", (isJuridical == 0 && person != null) ? person.getCitizenshipId() : null)
                .addValue("p_owner_gender", (isJuridical == 0 && person != null) ? person.getGender() : null)

                // General
                .addValue("p_owner_phone", insurant.getPhone())
                .addValue("p_owner_email", insurant.getEmail())
                .addValue("p_owner_address", insurant.getAddress())
                .addValue("p_owner_resident", insurant.getResidentType())

                // Juridical data (fill only if type == 1)
                .addValue("p_yur_inn", (isJuridical == 1 && org != null) ? org.getInn() : null)
                .addValue("p_yur_org_name", null)
                .addValue("p_yur_dir", null)
                // Object array (travelers)
                .addValue("p_travelers", generateTravelersArray(request.getTravelers()), Types.ARRAY);

        Map<String, Object> out = jdbcCall.execute(inParams);

        int errorCode = Integer.parseInt(String.valueOf(out.get("out_error")));
        String errorText = String.valueOf(out.get("out_error_text"));

        if (errorCode == 0 && out.get("return") != null) {
            long contractId = Long.parseLong(String.valueOf(out.get("return")));
            response = new UzumTravelContractResponse(0, "Success", contractId);
        } else {
            response = new UzumTravelContractResponse(errorCode != 0 ? errorCode : -1, errorText);
        }

        logService.addLog("PASSENGER_CREATE", request, response.getResult(), response.getResult_message(), response, response.getContractId());
        return response;
    }

    /**
     * Policy activation and generation
     */
    @Transactional
    public UzumTravelPolicyResponse activatePolicy(UzumTravelActivateRequest request) {

        UserEntity user = Utils.getUser();

        // 2. Verifying contract ownership.
        String checkSql = "SELECT COUNT(1) FROM INS_ANKETA WHERE INS_ID = ? AND USER_ID = ?";

        Integer exists = jdbcTemplate.queryForObject(
                checkSql,
                Integer.class,
                request.getContractId(),
                user.getTbId() // Current user ID
        );

        if (exists == null || exists == 0) {
            throw new ValidationException("Contract not found or you do not have permission to activate it.");
        }

        UzumTravelPolicyResponse response;

        final int HARDCODED_PAYMENT_TYPE = 2;

        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("FOR_PASSENGER_INSURANCE")
                .withFunctionName("ACTIVATE_AGENT_POLICY")
                .declareParameters(
                        new SqlOutParameter("policy_series", Types.VARCHAR),
                        new SqlOutParameter("policy_number", Types.NUMERIC),
                        new SqlOutParameter("error", Types.NUMERIC),
                        new SqlOutParameter("error_text", Types.VARCHAR),
                        new SqlOutParameter("policy_uuid", Types.VARCHAR));

        SqlParameterSource inParams = new MapSqlParameterSource()
                .addValue("p_contract_id", request.getContractId())
                .addValue("p_opl_type", HARDCODED_PAYMENT_TYPE);

        Map<String, Object> out = jdbcCall.execute(inParams);

        int result = Integer.parseInt(String.valueOf(out.get("error")));
        String errorText = String.valueOf(out.get("error_text"));

        if (result == 0) {
            String series = String.valueOf(out.get("policy_series"));
            String number = String.valueOf(out.get("policy_number"));
            String uuid = String.valueOf(out.get("policy_uuid"));
            String link = "https://ersp.e-osgo.uz/site/export-to-pdf?oldAlso=yes&id=" + uuid;

            response = new UzumTravelPolicyResponse(0, "Success", series, number, link);
        } else {
            response = new UzumTravelPolicyResponse(result, errorText);
        }

        Long contractId = request.getContractId() != null ? request.getContractId() : null;
        logService.addLog("PASSENGER_ACTIVATE", request, response.getResult(), response.getResult_message(), response,
                contractId);

        return response;
    }

    public UzumTravelGetPolicyResponse getPolicy(UzumTravelPolicyRequest request) {
        String sql = """
                    SELECT a.INS_ID AS contract_id,
                           a.INS_NUM AS contract_num,
                           p.TB_SERY AS tb_sery,
                           p.TB_NUMBER AS tb_number,
                           p.TB_STATUS AS tb_status,
                           p.FOND_UID AS uuid
                    FROM INS_ANKETA a
                    LEFT JOIN INS_POLIS p ON p.TB_ANKETA = a.INS_ID
                    WHERE a.INS_ID = ?
                    ORDER BY p.TB_ID DESC
                """;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, request.getContract_id());

//        if (rows.isEmpty()) {
//            return UzumTravelGetPolicyResponse.builder()
//                    .result(-404)
//                    .resultMessage("Contract not found")
//                    .build();
//        }

        if (rows.isEmpty()) {
            throw new PolicyNotFoundException(-404, "Contract not found");
        }

        Map<String, Object> row = rows.get(0);

        String contractNum = (String) row.get("contract_num");
        String tbSery = (String) row.get("tb_sery");
        Number tbNumber = (Number) row.get("tb_number");
        Number tbStatus = (Number) row.get("tb_status");
        String uuid = (String) row.get("uuid");

        String policyNo = null;
        if (tbSery != null && tbNumber != null) {
            policyNo = tbSery + " " + tbNumber;
        }

        String policyStatus = null;
        if (tbStatus != null) {
            int status = tbStatus.intValue();
            if (status == 2) {
                policyStatus = "ISSUED";
            } else if (status == 3 || status == 4) {
                policyStatus = "TERMINATED";
            } else {
                policyStatus = "CANCELLED";
            }
        }

        UzumTravelGetPolicyResponse.PolicyLink policyLink = new UzumTravelGetPolicyResponse.PolicyLink();
        if (uuid != null && "ISSUED".equals(policyStatus)) {
            String uz = BASE_URL + "/uz/site/export-to-pdf?id=" + uuid;
            String ru = BASE_URL + "/ru/site/export-to-pdf?id=" + uuid;
            String en = BASE_URL + "/en/site/export-to-pdf?id=" + uuid;
            policyLink = UzumTravelGetPolicyResponse.PolicyLink.builder()
                    .uz(uz)
                    .ru(ru)
                    .en(en)
                    .build();
        }

        return UzumTravelGetPolicyResponse.builder()
                .result(0)
                .resultMessage("Request processed successfully.")
                .contractId(request.getContract_id())
                .contractNum(contractNum)
                .policyNo(policyNo)
                .policyStatus(policyStatus)
                .policyLink(policyLink)
                .build();
    }

    /**
     * Program and risk data
     */
    private final Map<Integer, String> riskCache = new ConcurrentHashMap<>();
    private final Set<Integer> validRiskIds = ConcurrentHashMap.newKeySet();

    @PostConstruct
    public void initRisks() {
        refreshRiskCache();
    }

    @Scheduled(fixedRate = 3600000)
    public void refreshRiskCache() {
        log.info("Starting risk cache update...");
        try {
            String sql = "SELECT ID, RISK_CODE, RISK_NAME FROM UZUM_INS_PASSENGER_PROGRAMS WHERE DELETED = 0";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

            if (rows.isEmpty()) {
                log.warn("Warning: Risk table is empty. Cache not updated.");
                return;
            }

            Map<Integer, String> tempCache = new ConcurrentHashMap<>();
            Set<Integer> tempIds = ConcurrentHashMap.newKeySet();

            for (Map<String, Object> row : rows) {
                Integer id = ((Number) row.get("ID")).intValue();
                String code = (String) row.get("RISK_CODE");

                // Trim for reliability
                if (code != null) code = code.trim();

                tempCache.put(id, code);
                tempIds.add(id);
            }

            riskCache.clear();
            riskCache.putAll(tempCache);

            validRiskIds.clear();
            validRiskIds.addAll(tempIds);

            log.info("Risk cache updated successfully. Total entries: {}", riskCache.size());
        } catch (Exception e) {

            log.error("Critical error during risk dictionary update", e);
        }
    }

    private String convertRiskIdsToCodes(List<Integer> ids) {
        if (ids == null || ids.isEmpty())
            return "";

        List<Integer> uniqueIds = ids.stream().distinct().collect(Collectors.toList());
        if (uniqueIds.size() < ids.size()) {
            throw new ValidationException("Duplicate risk IDs detected: " + ids);
        }

        return uniqueIds.stream()
                .filter(riskCache::containsKey)
                .map(riskCache::get)
                .collect(Collectors.joining(","));
    }

    public boolean isValidRiskId(Integer id) {
        return validRiskIds.contains(id);
    }

    public List<Map<String, Object>> getAvailableRisksWithIds() {

        String sql = "SELECT ID as \"riskId\", RISK_CODE as \"code\", RISK_NAME as \"title\", " +
                "PREMIUM_ONE_WAY, PREMIUM_ROUND_TRIP, LIMIT_AMOUNT " +
                "FROM UZUM_INS_PASSENGER_PROGRAMS where DELETED=0 ORDER BY ID";
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * Premium calculator
     */
    @Transactional
    public UzumTravelCalculatorResponse calculatePremium(UzumTravelCalculatorRequest request) throws SQLException {

        if (request.getTravelers() != null) {
            for (UzumTravelCalculatorRequest.CalcTravelerDto t : request.getTravelers()) {
                if (t.getRiskIds() != null) {
                    for (Integer riskId : t.getRiskIds()) {
                        if (riskId == 6) {
                            throw new ValidationException("Risk ID 6 is not available for Uzum Travel.");
                        }
                        if (!isValidRiskId(riskId)) {
                            throw new ValidationException("A non-existent risk ID was specified: " + riskId +
                                    ". Available IDs can be obtained via /dictionaries");
                        }
                    }
                }
            }
        }

//        Double rate = jdbcTemplate.queryForObject("SELECT F_INS_GETKURS(2, TRUNC(SYSDATE)) FROM DUAL", Double.class);
//        if (rate == null || rate <= 0) {
//            throw new ValidationException("Error obtaining exchange rate. Calculation impossible. Please try again later.");
//        }

        Double rate = 1.0;

        List<TravelerDto> tempTravelers = new ArrayList<>();
        for (UzumTravelCalculatorRequest.CalcTravelerDto calcDto : request.getTravelers()) {
            TravelerDto t = new TravelerDto();
            t.setBirthDate(LocalDate.of(2000, 10, 10));;
            t.setRiskIds(calcDto.getRiskIds());
            t.setTripType(calcDto.getTripType());
            // Placeholders for required fields
            t.setPassportSeries("XX");
            t.setPassportNumber("0000000");
            t.setFirstName("CALC");
            t.setLastName("CALC");
            t.setMiddleName("");
            t.setGender(1);
            t.setCitizenshipId(182);
            t.setEmail("calc@calc.uz");
            tempTravelers.add(t);
        }

        // 3. Call CALCULATE_TOTAL_PREMIUM function
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("FOR_PASSENGER_INSURANCE")
                .withFunctionName("CALCULATE_TOTAL_PREMIUM")
                .declareParameters(
                        new SqlOutParameter("out_liability", Types.NUMERIC)
                );

        SqlParameterSource inParams = new MapSqlParameterSource()
                .addValue("p_contract_type", "UZUM_TRAVEL")
                .addValue("p_travelers", generateTravelersArray(tempTravelers), Types.ARRAY);

        Map<String, Object> out = jdbcCall.execute(inParams);

        // 4. Processing results
//        Number premUsdNum = (Number) out.get("return");
//        Number liabilityUsdNum = (Number) out.get("out_liability");
//
//        double premUsd = premUsdNum != null ? premUsdNum.doubleValue() : 0.0;
//        double liabilityUsd = liabilityUsdNum != null ? liabilityUsdNum.doubleValue() : 0.0;
//
//        // 5. Конвертация в сумы
//        double premUzs = Math.round(premUsd * rate);
//        double liabilityUzs = Math.round(liabilityUsd * rate);

//        return new UzumTravelCalculatorResponse(
//                0, "Success",
//                premUsd,
//                premUzs,
//                rate,
//                liabilityUsd
//        );

        Number premRaw = (Number) out.get("return");
        Number liabilityRaw = (Number) out.get("out_liability");

        double prem = premRaw != null ? premRaw.doubleValue() : 0.0;
        double liability = liabilityRaw != null ? liabilityRaw.doubleValue() : 0.0;

        // Since we switched to UZS, conversion is not needed (or multiply by 1)
        // But better to just return what came from the DB.

        return new UzumTravelCalculatorResponse(
                0,
                "Success",
                prem,      // premium (UZS)
                liability  // coverageAmount (UZS)
        );
    }


    /**
     * Helper method for List -> Oracle Array conversion
     */
    private Array generateTravelersArray(List<TravelerDto> travelers) throws SQLException {
        if (travelers == null || travelers.isEmpty()) {
            return null;
        }

        Connection connection = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());

        try {
            OracleConnection oracleConnection = connection.unwrap(OracleConnection.class);
            Struct[] structArray = new Struct[travelers.size()];
            for (int i = 0; i < travelers.size(); i++) {
                TravelerDto dto = travelers.get(i);

                Date sqlBirthDate;
                if (dto.getBirthDate() != null) {
                    sqlBirthDate = Date.valueOf(dto.getBirthDate());
                } else {
                    // Placeholder if date is missing (to avoid error)
                    sqlBirthDate = Date.valueOf(LocalDate.of(2000, 1, 1));
                }

                String risksString = convertRiskIdsToCodes(dto.getRiskIds());

                // Attribute order STRICTLY matches PL/SQL type PASSENGER_TRAVELER_OBJ
                Object[] attributes = new Object[]{
                        dto.getPassportSeries(),      // 1
                        dto.getPassportNumber(),      // 2
                        dto.getFirstName(),           // 3
                        dto.getLastName(),            // 4
                        dto.getMiddleName(),          // 5
                        dto.getGender(),              // 6
                        sqlBirthDate, // 7
                        dto.getCitizenshipId(),       // 8
                        risksString,           // 9
                        dto.getTripType(),            // 10
                        dto.getEmail(),                // 11
                        dto.getPinfl(),                // 12
                        dto.getResidentType(),         // 13
                        dto.getAddress(),              // 14
                        dto.getFlightNumber(),         // 15
                        dto.getPhone()                 // 16
                };

                structArray[i] = oracleConnection.createStruct("PASSENGER_TRAVELER_OBJ", attributes);
            }
            return oracleConnection.createOracleArray("PASSENGER_TRAVELERS", structArray);
        } finally {
            DataSourceUtils.releaseConnection(connection, jdbcTemplate.getDataSource());
        }
    }
}