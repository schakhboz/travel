package uz.insonline.travel.TravelV2.services;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
import uz.insonline.travel.TravelV2.payloads.requests.ContractRequestV2;
import uz.insonline.travel.TravelV2.payloads.requests.PolicyAnnulmentRequest;
import uz.insonline.travel.TravelV2.payloads.requests.PolicyCreateRequest;
import uz.insonline.travel.TravelV2.payloads.requests.PolicyRequestV2;
import uz.insonline.travel.TravelV2.payloads.responses.*;
import uz.insonline.travel.authentication.entity.Policy;
import uz.insonline.travel.authentication.entity.UserEntity;
import uz.insonline.travel.authentication.repository.AnketaRepository;
import uz.insonline.travel.authentication.repository.PolicyRepository;
import uz.insonline.travel.commons.exceptions.CreationException;
import uz.insonline.travel.commons.exceptions.PolicyNotFoundException;
import uz.insonline.travel.commons.payload.response.ApiResponseAll;
import uz.insonline.travel.commons.util.CustomUtil;
import uz.insonline.travel.log.service.LogService;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TravelServiceV2 {

    LogService logService;
    JdbcTemplate jdbcTemplate;
    PolicyRepository policyRepository;
    AnketaRepository anketaRepository;
    static String BASE_URL = "https://ersp.e-osgo.uz";

    @Autowired
    public void setSecretKey(@Value("${policy.secret-key}") String secretKey, @Value("${fond.policy-url}") String fondURL) {
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

    /*
        public String getCountries(List<String> countries) {
            StringBuilder result = new StringBuilder();
            if (countries == null || countries.isEmpty()) {
                return null;
            }
            for (int i = 0; i < countries.size(); i++) {
                result.append(countries.get(i).toUpperCase());
                if (i < countries.size() - 1) {
                    result.append(":");
                }
            }
            return result.toString();
        }
    */

    @Transactional
    public Array getTravelers(List<ContractRequestV2.TravelerData> travelerDataList) throws SQLException {
        if (travelerDataList == null || travelerDataList.isEmpty()) {
            return null;
        }

        assert jdbcTemplate.getDataSource() != null;
        Connection connection = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
        OracleConnection oracleConnection = connection.unwrap(OracleConnection.class);
        Struct[] travelers = new Struct[travelerDataList.size()];

        try {
            for (int i = 0; i < travelerDataList.size(); i++) {
                ContractRequestV2.TravelerData travelerData = travelerDataList.get(i);
                Object[] traveler = new Object[22];
                if (travelerData.getPerson().getResident() == 2) {
                    traveler[0] = travelerData.getPerson().getFirstName().trim();
                    traveler[1] = travelerData.getPerson().getLastName().trim();
                    traveler[2] = travelerData.getPerson().getMiddleName() == null ? "XXX" : travelerData.getPerson().getMiddleName().trim();
                    traveler[14] = travelerData.getPerson().getCitizenship();
                    traveler[15] = travelerData.getPerson().getGender();
                }
                traveler[3] = travelerData.getPerson().getPassport_series().trim();
                traveler[4] = travelerData.getPerson().getPassport_number().trim();
                traveler[7] = CustomUtil.toDate(travelerData.getPerson().getBirth_date(), "yyyy-MM-dd");
                traveler[13] = travelerData.getPerson().getResident();
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

    @Transactional
    public ApiResponseAll create(ContractRequestV2 dto) throws SQLException {
        ApiResponseAll response;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
//        dto.validateRequest(user.getTbId());

        if (dto.getDetails() != null) {
            if (dto.getDetails().getProgram_type() != null) {
                Integer programCount = jdbcTemplate.queryForObject("select count(*) from INS_ABROAD_PROGRAM where ins_id = ? and active = 1", Integer.class, dto.getDetails().getProgram_type());
                if (programCount == null || programCount == 0) {
                    return new ApiResponseAll(-1, "Invalid program_type. Such program does not exist");
                }
            }

            if (dto.getDetails().getActivity_type() != null) {
                Integer activityCount = jdbcTemplate.queryForObject("select count(*) from INS_ABROAD_ACTIVITY where ins_id = ?", Integer.class, dto.getDetails().getActivity_type());
                if (activityCount == null || activityCount == 0) {
                    return new ApiResponseAll(-1, "Invalid activity_type. Such activity does not exist");
                }
            }

            if (dto.getDetails().getGroup_type() != null) {
                Integer groupCount = jdbcTemplate.queryForObject("select count(*) from INS_ABROAD_GROUP where ins_id = ? and ins_active = 1", Integer.class, dto.getDetails().getGroup_type());
                if (groupCount == null || groupCount == 0) {
                    return new ApiResponseAll(-1, "Invalid group_type. Such group does not exist");
                }
            }

            if (dto.getDetails().getTravel_type() != null && dto.getDetails().getTravel_type() == 1 && dto.getDetails().getMulti_day_type() != null) {
                Integer multiCount = jdbcTemplate.queryForObject("select count(*) from INS_ABROAD_MULTI where ins_id = ? and ins_active = 1", Integer.class, dto.getDetails().getMulti_day_type());
                if (multiCount == null || multiCount == 0) {
                    return new ApiResponseAll(-1, "Invalid multi_day_type. Such type does not exist");
                }
            }
        }

        SimpleJdbcCall jdbcCall =
                new SimpleJdbcCall(jdbcTemplate)
                        .withCatalogName("FOR_TRAVEL_API_UPDATED");

        // prestige uses another method to generate policy at a time
        if (user.getTbId() == 2544) {
            jdbcCall.withFunctionName("CREATE_POLICY")
                    .declareParameters(
                            new SqlInOutParameter("policy_series", Types.VARCHAR),
                            new SqlInOutParameter("policy_number", Types.NUMERIC),
                            new SqlInOutParameter("policy_id", Types.NUMERIC),
                            new SqlInOutParameter("policy_uuid", Types.VARCHAR),
                            new SqlInOutParameter("contract_uuid", Types.VARCHAR),
                            new SqlOutParameter("out_error", Types.NUMERIC),
                            new SqlOutParameter("out_error_text", Types.VARCHAR)
                    );
            dto.getApplicant().getPerson().setPhone("998903462006");
            dto.getApplicant().getPerson().setEmail("it_prestigetravel@mail.ru");
        } else {
            jdbcCall.withFunctionName("CREATE_CONTRACT_FIZ_V2")
                    .declareParameters(
                            new SqlOutParameter("out_error", Types.INTEGER),
                            new SqlOutParameter("out_error_text", Types.VARCHAR),
                            new SqlOutParameter("commission", Types.VARCHAR),
                            new SqlOutParameter("policy_uuid", Types.VARCHAR)
                    );
        }

        /*
        if (dto.getApplicant().getIs_applicant_traveler() == 1) {
            dto.getTravelers().add(new ContractRequest.TravelerData(dto.getApplicant().getPerson()));
        }
        */

        SqlParameterSource in = new MapSqlParameterSource()
                .addValue("startDate", CustomUtil.toDate(dto.getDetails().getStart_date(), "yyyy-MM-dd"), Types.DATE)
                .addValue("days", dto.getDetails().getTravel_days())
                .addValue("travel_type", dto.getDetails().getTravel_type())
                .addValue("multiId", dto.getDetails().getMulti_day_type())
                .addValue("programId", dto.getDetails().getProgram_type())
                .addValue("activityId", dto.getDetails().getActivity_type())
                .addValue("groupId", dto.getDetails().getGroup_type())
                .addValue("countries", dto.getDetails().getCountries() != null ? getCountries(dto.getDetails().getCountries()) : null)
                .addValue("app_resident", dto.getApplicant().getPerson().getResident())
                .addValue("app_email", dto.getApplicant().getPerson().getResident() == 2 ? dto.getApplicant().getPerson().getEmail() : null)
                .addValue("app_citizenship", dto.getApplicant().getPerson().getResident() == 2 ? dto.getApplicant().getPerson().getCitizenship() : null)
                .addValue("app_firstname", dto.getApplicant().getPerson().getResident() == 2 ? dto.getApplicant().getPerson().getFirstName() : null)
                .addValue("app_lastname", dto.getApplicant().getPerson().getResident() == 2 ? dto.getApplicant().getPerson().getLastName() : null)
                .addValue("app_middlename", dto.getApplicant().getPerson().getResident() == 2 ? dto.getApplicant().getPerson().getMiddleName() != null ? dto.getApplicant().getPerson().getMiddleName() : "XXX" : null)
                .addValue("app_gender", dto.getApplicant().getPerson().getGender())
                .addValue("app_Date_Birth", CustomUtil.toDate(dto.getApplicant().getPerson().getBirth_date(), "yyyy-MM-dd"), Types.DATE)
                .addValue("app_Pass_Series", dto.getApplicant().getPerson().getPassport_series().trim())
                .addValue("app_Pass_Num", dto.getApplicant().getPerson().getPassport_number().trim())
                .addValue("app_Phone", dto.getApplicant().getPerson().getPhone() != null ? dto.getApplicant().getPerson().getPhone().trim() : null)
                .addValue("userId", user.getTbId())
                .addValue("people", getTravelers(dto.getTravelers()), Types.ARRAY)
                .addValue("transactionId", dto.getTransactionId());

        Map<String, Object> out = jdbcCall.execute(in);
        int result = Integer.parseInt(String.valueOf(out.get("out_error")));
        String errText = CustomUtil.cleanOracleError(String.valueOf(out.get("out_error_text")));
        Long contractId = null;

        if (result == 0) {
            contractId = Long.parseLong(String.valueOf(out.get("return")));
            if (user.getTbId() == 2544) {
                long policyId = Long.parseLong(String.valueOf(out.get("policy_id")));
                String policySeries = String.valueOf(out.get("policy_series"));
                String policyNumber = String.valueOf(out.get("policy_number"));
                String commission = String.valueOf(out.get("commission"));
                String uuid = String.valueOf(out.get("policy_uuid"));
                String policy = "https://api-travel.insonline.uz/api/travel/policy-old/%s/%s";
//                String encodedString = Base64.getEncoder().encodeToString(policy.bytes());
                policy = String.format(policy, policyId, contractId);
//                policy = String.valueOf(out.get("policy_uuid"));
                String contractUuid = String.valueOf(out.get("contract_uuid"));
                response = new PolicyResponse(contractId, contractUuid, policySeries, policyNumber, policyId, policy, result, errText);
            } else
                response = new ContractResponse(result, errText, contractId);
        } else {
            if (user.getTbId() == 2544)
                response = new PolicyResponse(result, errText);
            else
                response = new ContractResponse(result, errText);
        }

        logService.addLog("TRAVEL_CREATE_V2", dto, response.getResult(), response.getResult_message(), response, contractId);

        if (response.getResult() != 0) {
            throw new CreationException(response, HttpStatus.BAD_REQUEST);
        }
        return response;
    }

    public PolicyCreateResponse payment(PolicyCreateRequest dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity users = (UserEntity) authentication.getPrincipal();
        PolicyCreateResponse response;
        SimpleJdbcCall jdbcCall =
                new SimpleJdbcCall(jdbcTemplate)
                        .withCatalogName("FOR_TRAVEL_API_UPDATED")
                        .withFunctionName("Generate_Policy_v2")
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
                return new PolicyCreateResponse(-2, "Contract not found!", null, null);

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

    public GetPolicyResponse getPolicy(PolicyRequestV2 request) {
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
//            return GetPolicyResponse.builder()
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

        GetPolicyResponse.PolicyLink policyLink = new GetPolicyResponse.PolicyLink();
        if (uuid != null && "ISSUED".equals(policyStatus)) {
            String uz = BASE_URL + "/uz/site/export-to-pdf?id=" + uuid;
            String ru = BASE_URL + "/ru/site/export-to-pdf?id=" + uuid;
            String en = BASE_URL + "/en/site/export-to-pdf?id=" + uuid;
            policyLink = GetPolicyResponse.PolicyLink.builder()
                    .uz(uz)
                    .ru(ru)
                    .en(en)
                    .build();
        }

        return GetPolicyResponse.builder()
                .result(0)
                .resultMessage("Request processed successfully.")
                .contractId(request.getContract_id().longValue())
                .contractNum(contractNum)
                .policyNo(policyNo)
                .policyStatus(policyStatus)
                .policyLink(policyLink)
                .build();
    }

    public PolicyAnnulmentResponse policyAnnulment(PolicyAnnulmentRequest dto) {
        if (dto.getReasonNumber() == 0 && (dto.getReasonText() == null || dto.getReasonText().isBlank())) {
            return new PolicyAnnulmentResponse(400, "Reason number is 0, reasonText can not be null");
        }
        PolicyAnnulmentResponse response;
        SimpleJdbcCall jdbcCall =
                new SimpleJdbcCall(jdbcTemplate)
                        .withCatalogName("FOR_TRAVEL_API_UPDATED")
                        .withFunctionName("POLICY_ANNULMENT")
                        .declareParameters(
                                new SqlOutParameter("error", Types.NUMERIC),
                                new SqlOutParameter("error_text", Types.VARCHAR)
                        );

        SqlParameterSource in = new MapSqlParameterSource()
                .addValue("contract_id", Integer.valueOf(dto.getContractId()))
                .addValue("reason_number", dto.getReasonNumber())
                .addValue("reason_text", dto.getReasonText());

        Map<String, Object> out = jdbcCall.execute(in);

        int result = Integer.parseInt(String.valueOf(out.get("error")));
        String errText = String.valueOf(out.get("error_text"));

        if (result == 0 && out.get("return") != null) {
            response = new PolicyAnnulmentResponse(result, errText);
            return response;
        }
        response = new PolicyAnnulmentResponse(result, errText);
        logService.addLog("TRAVEL_TERMINATE", dto, response.getResult(), response.getResult_message(), response, Long.valueOf(dto.getContractId()));
        if (response.getResult() != 0) {
            throw new CreationException(new ApiResponseAll(response.getResult(), response.getResult_message()), HttpStatus.BAD_REQUEST);
        }
        return response;
    }
}
