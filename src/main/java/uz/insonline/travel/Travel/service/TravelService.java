package uz.insonline.travel.Travel.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.OracleConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlInOutParameter;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.insonline.travel.Travel.payload.dto.PolicySerialNumber;
import uz.insonline.travel.Travel.payload.request.PolicyCreateRequest;
import uz.insonline.travel.Travel.payload.request.main.ContractRequest;
import uz.insonline.travel.Travel.payload.request.main.TravelCalculatorRequest;
import uz.insonline.travel.Travel.payload.response.CalculatorResponse;
import uz.insonline.travel.Travel.payload.response.ContractResponse;
import uz.insonline.travel.Travel.payload.response.FileResponse;
import uz.insonline.travel.Travel.payload.response.PolicyCreateResponse;
import uz.insonline.travel.authentication.entity.Policy;
import uz.insonline.travel.authentication.entity.UserEntity;
import uz.insonline.travel.authentication.repository.PolicyRepository;
import uz.insonline.travel.commons.exceptions.CreationException;
import uz.insonline.travel.commons.exceptions.PolicyNotFoundException;
import uz.insonline.travel.commons.exceptions.PolicyNotGivenException;
import uz.insonline.travel.commons.payload.response.ApiResponse;
import uz.insonline.travel.commons.payload.response.ApiResponseAll;
import uz.insonline.travel.commons.util.CustomUtil;
import uz.insonline.travel.commons.util.EncryptionService;
import uz.insonline.travel.log.service.LogService;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TravelService {

    private String secretKey;
    private final LogService logService;
    private final JdbcTemplate jdbcTemplate;
    private final PolicyRepository policyRepository;
    private EncryptionService encryptionService;

    @Autowired
    public void setSecretKey(@Value("${policy.secret-key}") String secretKey) {
        try {
            encryptionService = new EncryptionService(secretKey);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public CalculatorResponse calculator(TravelCalculatorRequest dto) {
        dto.validateRequest();
        CalculatorResponse response;
        int error_code;
        String error_msg;

        if (dto.getProgram_type() != null) {
            Integer programCount = jdbcTemplate.queryForObject("select count(*) from INS_ABROAD_PROGRAM where ins_id = ? and active = 1", Integer.class, dto.getProgram_type());
            if (programCount == null || programCount == 0) {
                return new CalculatorResponse(-1, "Invalid program_type. Such program does not exist");
            }
        }

        if (dto.getActivity_type() != null) {
            Integer activityCount = jdbcTemplate.queryForObject("select count(*) from INS_ABROAD_ACTIVITY where ins_id = ?", Integer.class, dto.getActivity_type());
            if (activityCount == null || activityCount == 0) {
                return new CalculatorResponse(-1, "Invalid activity_type. Such activity does not exist");
            }
        }

        if (dto.getGroup_type() != null) {
            Integer groupCount = jdbcTemplate.queryForObject("select count(*) from INS_ABROAD_GROUP where ins_id = ? and ins_active = 1", Integer.class, dto.getGroup_type());
            if (groupCount == null || groupCount == 0) {
                return new CalculatorResponse(-1, "Invalid group_type. Such group does not exist");
            }
        }

        if (dto.getTravel_type() != null && dto.getTravel_type() == 1 && dto.getMulti_day_type() != null) {
            Integer multiCount = jdbcTemplate.queryForObject("select count(*) from INS_ABROAD_MULTI where ins_id = ? and ins_active = 1", Integer.class, dto.getMulti_day_type());
            if (multiCount == null || multiCount == 0) {
                return new CalculatorResponse(-1, "Invalid multi_day_type. Such type does not exist");
            }
        }

        try {
            SimpleJdbcCall jdbcCall =
                    new SimpleJdbcCall(jdbcTemplate)
                            .withCatalogName("FOR_TRAVEL_API_UPDATED")
                            .withFunctionName("advanced_calculator_fiz")
                            .declareParameters(
                                    new SqlOutParameter("liability", Types.NUMERIC),
                                    new SqlOutParameter("curs", Types.NUMERIC),
                                    new SqlOutParameter("premium_uzs", Types.NUMERIC),
                                    new SqlOutParameter("out_error_code", Types.NUMERIC),
                                    new SqlOutParameter("out_error_msg", Types.VARCHAR),
                                    new SqlInOutParameter("people", Types.ARRAY, "INSURED_PERSON_TRAVEL")
                            );

            SqlParameterSource in = new MapSqlParameterSource()
                    .addValue("travel_type", dto.getTravel_type())
                    .addValue("activity_type", dto.getActivity_type())
                    .addValue("group_type", dto.getGroup_type())
                    .addValue("program_type", dto.getProgram_type())
                    .addValue("days", dto.getTravel_days())
                    .addValue("multi_type", dto.getMulti_day_type())
                    .addValue("people", getTravelersDOB(dto.getTravelers()), Types.ARRAY);

            Map<String, Object> out = jdbcCall.execute(in);
            error_msg = ((String) out.get("out_error_msg"));
            error_code = ((BigDecimal) out.get("out_error_code")).intValue();

            if (error_code == 0) {
                double insurancePremiumUZS = ((BigDecimal) out.get("premium_uzs")).doubleValue();
                double insurancePremiumUSD = ((BigDecimal) out.get("return")).doubleValue();
                double insuranceLiabilityUSD = ((BigDecimal) out.get("liability")).doubleValue();
                double curs = ((BigDecimal) out.get("curs")).doubleValue();
                response = new CalculatorResponse(error_code, error_msg, insurancePremiumUZS, insurancePremiumUSD, Math.round(insuranceLiabilityUSD * curs), insuranceLiabilityUSD, curs);
            } else
                response = new CalculatorResponse(-1, error_msg);

        } catch (Exception e) {
            response = new CalculatorResponse(-1, e.getMessage());
        }

        logService.addLog("TRAVEL_CALCULATOR", dto, response.getResult(), response.getResult_message(), response, null);
        return response;
    }

    public String getCountries(List<String> countries) {
        if (countries == null || countries.isEmpty()) {
            return null;
        }
        return countries.stream()
                .filter(Objects::nonNull)
                .map(String::toUpperCase)
                .collect(Collectors.joining(":"));
    }

//    public String getCountries(List<String> countries) {
//        StringBuilder result = new StringBuilder();
//        if (countries == null || countries.isEmpty()) {
//            return null;
//        }
//        for (int i = 0; i < countries.size(); i++) {
//            result.append(countries.get(i));
//            if (i < countries.size() - 1) {
//                result.append(":");
//            }
//        }
//        return result.toString();
//    }

    @Transactional
    public Array getTravelers(List<ContractRequest.TravelerData> travelerDataList) throws SQLException {
        if (travelerDataList == null || travelerDataList.isEmpty()) {
            return null;
        }

        Connection connection = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
        OracleConnection oracleConnection = connection.unwrap(OracleConnection.class);
        Struct[] travelers = new Struct[travelerDataList.size()];

        try {
            for (int i = 0; i < travelerDataList.size(); i++) {
                ContractRequest.TravelerData travelerData = travelerDataList.get(i);
                Object[] traveler = new Object[18];
                traveler[3] = travelerData.getPerson().getPassport_series().trim();
                traveler[4] = travelerData.getPerson().getPassport_number().trim();
                traveler[7] = CustomUtil.toDate(travelerData.getPerson().getBirth_date(), "yyyy-MM-dd");
                travelers[i] = oracleConnection.createStruct("INSURED_ROW", traveler);
            }

            return oracleConnection.createOracleArray("INSURED_PERSON_TRAVEL", travelers);
        } catch (SQLException e) {
            // Log the error message
            System.err.println("SQLException: " + e.getMessage());
            DataSourceUtils.releaseConnection(connection, jdbcTemplate.getDataSource());
            throw e;
        } finally {
            DataSourceUtils.releaseConnection(connection, jdbcTemplate.getDataSource());
        }
    }


    public Array getTravelersDOB(List<TravelCalculatorRequest.TravelerCalculatorData> travelersDOBs) throws SQLException {
        Struct[] travelers = null;
        Connection connection = DataSourceUtils.getConnection(Objects.requireNonNull(jdbcTemplate.getDataSource()));
        try {
            OracleConnection oracleConnection = connection.unwrap(OracleConnection.class);

            if (travelersDOBs != null && !travelersDOBs.isEmpty()) {
                travelers = new Struct[travelersDOBs.size()];
                int travelersIndex = 0;
                for (TravelCalculatorRequest.TravelerCalculatorData travelerData : travelersDOBs) {
                    Object[] traveler = new Object[18];
                    traveler[7] = CustomUtil.toDate(travelerData.getBirth_date(), "yyyy-MM-dd");
                    travelers[travelersIndex] = oracleConnection.createStruct("INSURED_ROW", traveler);
                    travelersIndex++;
                }
            }
            return oracleConnection.createOracleArray("INSURED_PERSON_TRAVEL", travelers);
        } finally {
            DataSourceUtils.releaseConnection(connection, jdbcTemplate.getDataSource());
        }
    }

    @Transactional
    public ContractResponse create(ContractRequest dto) throws SQLException {
        dto.validateIndividual();
        ContractResponse response;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity users = (UserEntity) authentication.getPrincipal();

        if (dto.getDetails() != null) {
            if (dto.getDetails().getProgram_type() != null) {
                Integer programCount = jdbcTemplate.queryForObject("select count(*) from INS_ABROAD_PROGRAM where ins_id = ? and active = 1", Integer.class, dto.getDetails().getProgram_type());
                if (programCount == null || programCount == 0) {
                    return new ContractResponse(-1, "Invalid program_type. Such program does not exist");
                }
            }

            if (dto.getDetails().getActivity_type() != null) {
                Integer activityCount = jdbcTemplate.queryForObject("select count(*) from INS_ABROAD_ACTIVITY where ins_id = ?", Integer.class, dto.getDetails().getActivity_type());
                if (activityCount == null || activityCount == 0) {
                    return new ContractResponse(-1, "Invalid activity_type. Such activity does not exist");
                }
            }

            if (dto.getDetails().getGroup_type() != null) {
                Integer groupCount = jdbcTemplate.queryForObject("select count(*) from INS_ABROAD_GROUP where ins_id = ? and ins_active = 1", Integer.class, dto.getDetails().getGroup_type());
                if (groupCount == null || groupCount == 0) {
                    return new ContractResponse(-1, "Invalid group_type. Such group does not exist");
                }
            }

            if (dto.getDetails().getTravel_type() != null && dto.getDetails().getTravel_type() == 1 && dto.getDetails().getMulti_day_type() != null) {
                Integer multiCount = jdbcTemplate.queryForObject("select count(*) from INS_ABROAD_MULTI where ins_id = ? and ins_active = 1", Integer.class, dto.getDetails().getMulti_day_type());
                if (multiCount == null || multiCount == 0) {
                    return new ContractResponse(-1, "Invalid multi_day_type. Such type does not exist");
                }
            }
        }

        SimpleJdbcCall jdbcCall =
                new SimpleJdbcCall(jdbcTemplate)
                        .withCatalogName("FOR_TRAVEL_API_UPDATED")
                        .withFunctionName("CREATE_CONTRACT_FIZ")
                        .declareParameters(
                                new SqlOutParameter("out_error", Types.INTEGER),
                                new SqlOutParameter("out_error_text", Types.VARCHAR)
                        );

//        if (dto.getApplicant().getIs_applicant_traveler() == 1) {
//            dto.getTravelers().add(new ContractRequest.TravelerData(dto.getApplicant().getPerson()));
//        }

        SqlParameterSource in = new MapSqlParameterSource()
                .addValue("startDate", CustomUtil.toDate(dto.getDetails().getStart_date(), "yyyy-MM-dd"), Types.DATE)
                .addValue("days", dto.getDetails().getTravel_days())
                .addValue("travel_type", dto.getDetails().getTravel_type())
                .addValue("multiId", dto.getDetails().getMulti_day_type())
                .addValue("programId", dto.getDetails().getProgram_type())
                .addValue("activityId", dto.getDetails().getActivity_type())
                .addValue("groupId", dto.getDetails().getGroup_type())
                .addValue("countries", getCountries(dto.getDetails().getCountries()))
                .addValue("app_Fiz_yur", dto.isApplicantIndividual() ? 0 : 1)
                .addValue("app_Date_Birth", CustomUtil.toDate(dto.getApplicant().getPerson().getBirth_date(), "yyyy-MM-dd"), Types.DATE)
                .addValue("app_Pass_Series", dto.getApplicant().getPerson().getPassport_series().trim())
                .addValue("app_Pass_Num", dto.getApplicant().getPerson().getPassport_number().trim())
                .addValue("app_Phone", dto.getApplicant().getPhone().trim())
                .addValue("userId", users.getTbId())
                .addValue("people", getTravelers(dto.getTravelers()), Types.ARRAY);

        Map<String, Object> out = jdbcCall.execute(in);
        Object outErrObj = out.get("out_error");
        int result = (outErrObj != null && !"null".equalsIgnoreCase(String.valueOf(outErrObj))) ? Integer.parseInt(String.valueOf(outErrObj)) : -1;
        String errText = uz.insonline.travel.commons.util.CustomUtil.cleanOracleError(String.valueOf(out.get("out_error_text")));
        Long anketaId = null;
        if (result == 0) {
            Object returnObj = out.get("return");
            long contractId = (returnObj != null && !"null".equalsIgnoreCase(String.valueOf(returnObj))) ? Long.parseLong(String.valueOf(returnObj)) : 0L;
            anketaId = contractId;

            response = new ContractResponse(result, errText, contractId);
        } else
            response = new ContractResponse(result, errText);

        logService.addLog("TRAVEL_CREATE", dto, response.getResult(), response.getResult_message(), response, anketaId);
        if (response.getResult() != 0)
            throw new CreationException(new ApiResponseAll(response.getResult(), response.getResult_message()), HttpStatus.BAD_REQUEST);

        return response;
    }

    public PolicyCreateResponse payment(PolicyCreateRequest dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity users = (UserEntity) authentication.getPrincipal();
        PolicyCreateResponse response;
        SimpleJdbcCall jdbcCall =
                new SimpleJdbcCall(jdbcTemplate)
                        .withCatalogName("FOR_TRAVEL_API_UPDATED")
                        .withFunctionName("Generate_Policy")
                        .declareParameters(
                                new SqlOutParameter("policy_series", Types.VARCHAR),
                                new SqlOutParameter("policy_number", Types.INTEGER),
                                new SqlOutParameter("error", Types.INTEGER),
                                new SqlOutParameter("error_text", Types.VARCHAR),
                                new SqlOutParameter("policy_uuid", Types.VARCHAR)
                        );

        SqlParameterSource in = new MapSqlParameterSource()
                .addValue("contract_id", dto.getContractID());

        Map<String, Object> out = jdbcCall.execute(in);

        int result = Integer.parseInt(String.valueOf(out.get("error")));
        String errText = String.valueOf(out.get("error_text"));

        if (result == 0 && out.get("return") != null) {
            Optional<Policy> optionalPolicy = policyRepository.findByAnketaIdAndUserId(Long.valueOf(dto.getContractID()), users.getTbId());
            if (optionalPolicy.isEmpty()) {
                return new PolicyCreateResponse(-2, "Policy not found!", null, null);
            }
            response = new PolicyCreateResponse(0, "Successfully completed!", new PolicyCreateResponse.Policy(String.valueOf(out.get("policy_series")), (String.valueOf(out.get("policy_number")))),
                    "https://ersp.e-osgo.uz/site/export-to-pdf?oldAlso=yes&id=" + out.get("policy_uuid"));
            return response;
        }
        response = new PolicyCreateResponse(result, errText);
        logService.addLog("TRAVEL_PAYMENT", dto, response.getResult(), response.getResult_message(), response, Long.valueOf(dto.getContractID()));
        if (response.getResult() != 0) {
            throw new CreationException(new ApiResponseAll(response.getResult(), response.getResult_message()), HttpStatus.BAD_REQUEST);
        }
        return response;
    }

    @SneakyThrows
    public PolicyCreateResponse getPolicyLink(Long contractId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity users = (UserEntity) authentication.getPrincipal();

        Optional<Policy> optionalPolicy = policyRepository.findByAnketaIdAndUserId(contractId, users.getTbId());
        if (optionalPolicy.isEmpty()) {
            throw new PolicyNotFoundException(-2, "Policy not found");
        }
        Policy policy = optionalPolicy.get();
        if (policy.getPolicyNumber() < 0) {
            throw new PolicyNotGivenException(-3, "Policy not given");
        }
        String link = "https://api-travel.insonline.uz/api/travel/policy?key=" + URLEncoder.encode(encryptionService.encrypt(policy.getPolicySery() + ":" + policy.getPolicyNumber()), StandardCharsets.UTF_8);
        String newLink = "https://ersp.e-osgo.uz/site/export-to-pdf?oldAlso=yes&id=" + policy.getFondUid();
        return new PolicyCreateResponse(0, "Successfully completed!", null, link, newLink);
    }

    public FileResponse getPolicy(String hashKey) {
        try {
            String decode = hashKey.replace(" ", "+");
            String decryptedString = encryptionService.decrypt(decode);
            String[] parts = decryptedString.split(":");

            if (parts.length != 2)
                return new FileResponse(-3, "Key is wrong format!", null, true, null);

            Optional<Policy> optionalPolicy = policyRepository.findByPolicySeryAndPolicyNumber(parts[0], Long.valueOf(parts[1]));
            if (optionalPolicy.isEmpty()) {
                return new FileResponse(-2, "Policy not found!", null, true);
            }

            SqlParameterSource in = new MapSqlParameterSource()
                    .addValue("p_seria", parts[0])
                    .addValue("p_number", parts[1]);

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("Generate_epolis_pdf_SER_NUM")
                    .declareParameters(
                            new SqlOutParameter("v_blob", Types.BLOB)
                    );

            Map<String, Object> out = jdbcCall.execute(in);
            Blob blob = (Blob) out.get("v_blob");
            if (blob == null)
                return new FileResponse(-1, "Policy not found", null, true);

            byte[] bytes = blob.getBytes(1, (int) blob.length());
            return new FileResponse(0, "Successfully completed!", bytes, false, parts[0] + parts[1]);

        } catch (Exception e) {
            throw new CreationException(new ApiResponseAll(-1, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @SneakyThrows
    public FileResponse getOldPolicy(Long policyId, Long contractId) {
        try {
            Optional<Policy> optionalPolicy = policyRepository.findByTbIdAndAnketaIdAndStatus(policyId, contractId, 2L);
            if (optionalPolicy.isEmpty()) {
                logService.addLog("TRAVEL_OLD_POLICY", new PolicySerialNumber(policyId, contractId), -1, "Active policy not found", contractId);
                return new FileResponse(-3, "Policy not found!", null, true);
            }
            Policy policy = optionalPolicy.get();
            String policySery = policy.getPolicySery();
            Long policyNumber = policy.getPolicyNumber();
            SqlParameterSource in = new MapSqlParameterSource()
                    .addValue("p_seria", policySery)
                    .addValue("p_number", policyNumber);

            SimpleJdbcCall jdbcCall =
                    new SimpleJdbcCall(jdbcTemplate)
                            .withProcedureName("Generate_epolis_pdf_SER_NUM")
                            .declareParameters(
                                    new SqlOutParameter("v_blob", Types.BLOB)
                            );
            Map<String, Object> out = jdbcCall.execute(in);
            Blob blob = (Blob) out.get("v_blob");
            if (blob == null)
                return new FileResponse(-1, "Policy not found", null, true);

            byte[] bytes = blob.getBytes(1, (int) blob.length());
            return new FileResponse(0, "Successfully completed!", bytes, false, policySery + policyNumber);

        } catch (Exception e) {
            logService.addLog("TRAVEL_OLD_POLICY", new PolicySerialNumber(policyId, contractId), -2, e.getMessage(), contractId);
            throw new CreationException(new ApiResponseAll(-1, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

}
