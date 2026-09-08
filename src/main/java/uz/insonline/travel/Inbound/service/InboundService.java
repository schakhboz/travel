package uz.insonline.travel.Inbound.service;

import oracle.jdbc.OracleConnection;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import uz.insonline.travel.authentication.entity.Policy;
import uz.insonline.travel.authentication.entity.UserEntity;
import uz.insonline.travel.authentication.repository.PolicyRepository;
import uz.insonline.travel.Inbound.payload.request.InboundCalcRequest;
import uz.insonline.travel.Inbound.payload.request.InboundCreateRequest;
import uz.insonline.travel.Inbound.payload.request.InboundTravelerRequest;
import uz.insonline.travel.Inbound.payload.response.CalculatorResponse;
import uz.insonline.travel.Travel.payload.response.ContractResponse;
import uz.insonline.travel.Travel.payload.response.PolicyCreateResponse;
import uz.insonline.travel.Inbound.payload.response.InboundProgramResponse;
import uz.insonline.travel.commons.exceptions.PolicyNotFoundException;
import uz.insonline.travel.log.service.LogService;

import java.sql.*;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class InboundService {

    private final LogService logService;
    private final JdbcTemplate jdbcTemplate;
    private final PolicyRepository policyRepository;

    public List<InboundProgramResponse> getPrograms() {
        String sql = "SELECT PROGRAM_ID, PROGRAM_NAME, COVERAGE_AMOUNT, DAILY_RATE " +
                "FROM INS_INBOUND_PROGRAMS " +
                "WHERE IS_ACTIVE = 1 ORDER BY PROGRAM_ID";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> new InboundProgramResponse(
                    rs.getLong("PROGRAM_ID"),
                    rs.getString("PROGRAM_NAME"),
                    rs.getDouble("COVERAGE_AMOUNT"),
                    rs.getDouble("DAILY_RATE")
            ));
        } catch (Exception e) {
            log.error("Error getting programs", e);
            return new ArrayList<>();
        }
    }


    public CalculatorResponse calculator(InboundCalcRequest dto) {
        CalculatorResponse response;
        try {
            String sql = "BEGIN ? := FOR_INBOUND_TRAVEL.CALCULATE_INBOUND_PREMIUM(?, ?, ?, ?); END;";

            List<SqlParameter> params = List.of(
                    new SqlOutParameter("return_premium", Types.NUMERIC),
                    new SqlParameter(Types.NUMERIC),
                    new SqlParameter(Types.NUMERIC),
                    new SqlParameter(Types.NUMERIC),
                    new SqlOutParameter("out_liability", Types.NUMERIC)
            );

            Map<String, Object> out = jdbcTemplate.call(conn -> {
                CallableStatement cs = conn.prepareCall(sql);
                cs.registerOutParameter(1, Types.NUMERIC);
                cs.setLong(2, dto.getProgramId());
                cs.setInt(3, dto.getDays());
                cs.setInt(4, dto.getTravelersCount());
                cs.registerOutParameter(5, Types.NUMERIC);
                return cs;
            }, params);

            BigDecimal premiumBd = (BigDecimal) out.get("return_premium");
            BigDecimal liabilityBd = (BigDecimal) out.get("out_liability");

            double premium = premiumBd != null ? premiumBd.doubleValue() : 0.0;
            double liability = liabilityBd != null ? liabilityBd.doubleValue() : 0.0;

            response = new CalculatorResponse(0, "Success", premium, liability);

        } catch (Exception e) {
            log.error("Calculator error: ", e);
            response = new CalculatorResponse(-1, e.getMessage());
        }
        return response;
    }


    private java.sql.Array createTravelersArray(List<InboundTravelerRequest> travelersList) {
        if (travelersList == null || travelersList.isEmpty()) {
            return null;
        }

        return jdbcTemplate.execute((ConnectionCallback<java.sql.Array>) con -> {
            OracleConnection oracleConnection;
            if (con.isWrapperFor(OracleConnection.class)) {
                oracleConnection = con.unwrap(OracleConnection.class);
            } else {
                throw new SQLException("Не удалось получить OracleConnection из пула");
            }

            Struct[] structs = new Struct[travelersList.size()];

            for (int i = 0; i < travelersList.size(); i++) {
                InboundTravelerRequest t = travelersList.get(i);
                Object[] attributes = new Object[]{
                        t.getFirstName(),       // 1. Name
                        t.getLastName(),        // 2. Surname
                        t.getMiddleName(),      // 3. Middle
                        t.getPassportSeries(),  // 4. Pass Sery
                        t.getPassportNumber(),  // 5. Pass Num
                        null,                   // 6. PINFL (пусто)
                        t.getPhone(),           // 7. Phone
                        t.getBirthDate() != null ? java.sql.Date.valueOf(t.getBirthDate()) : null,
                        null,                   // 9. Address
                        t.getEmail(),           // 10. Email
                        null,                   // 11. Region
                        null,                   // 12. District
                        null,                   // 13. Phone2
                        2,                      // 14. Resident ID (2 = иностранец)
                        t.getCitizenshipId(),   // 15. Citizenship
                        t.getGender()           // 16. Gender
                };
                structs[i] = oracleConnection.createStruct("INBOUND_INSURED_ROW", attributes);
            }
            return oracleConnection.createOracleArray("INBOUND_INSURED_TABLE", structs);
        });
    }


    @Transactional(rollbackFor = Exception.class)
    public ContractResponse create(InboundCreateRequest dto) {
        ContractResponse response;
        Long contractId = null;

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();

            InboundCreateRequest.ContractDetails details = dto.getDetails();
            InboundCreateRequest.ApplicantBlock applicant = dto.getApplicant();
            Integer fizYurType = applicant.getType(); 

            InboundCreateRequest.PersonData person = applicant.getPerson() != null ? applicant.getPerson() : new InboundCreateRequest.PersonData();
            InboundCreateRequest.CompanyData company = applicant.getCompany() != null ? applicant.getCompany() : new InboundCreateRequest.CompanyData();

            List<InboundTravelerRequest> finalTravelersList = new ArrayList<>();

            if (Boolean.TRUE.equals(applicant.getIsApplicantTraveler()) && fizYurType == 0 && applicant.getPerson() != null) {
                
                if (person.getResidentId() == null || person.getResidentId() != 2) {
                    return new ContractResponse(-1, "Validation Error: If the applicant is a traveler, residentId must be 2 (non-resident).");
                }

                InboundTravelerRequest ownerAsTraveler = new InboundTravelerRequest();
                ownerAsTraveler.setFirstName(person.getFirstName());
                ownerAsTraveler.setLastName(person.getLastName());
                ownerAsTraveler.setMiddleName(person.getMiddleName());
                ownerAsTraveler.setBirthDate(person.getBirthDate());
                ownerAsTraveler.setPassportSeries(person.getPassportSeries());
                ownerAsTraveler.setPassportNumber(person.getPassportNumber());
                ownerAsTraveler.setCitizenshipId(person.getCitizenshipId());
                ownerAsTraveler.setPhone(person.getPhone());
                ownerAsTraveler.setGender(person.getGender());
                ownerAsTraveler.setEmail(person.getEmail());

                finalTravelersList.add(ownerAsTraveler);
            }

            if (dto.getTravelers() != null) {
                finalTravelersList.addAll(dto.getTravelers());
            }

            if (finalTravelersList.isEmpty()) {
                return new ContractResponse(-1, "Список путешественников не может быть пустым");
            }

            String ownerEmail;
            String ownerPhone;
            String ownerPinfl = null;
            Long ownerRegion = null;
            Long ownerDistrict = null;
            Long ownerResident;

            if (fizYurType == 0) { 
                ownerEmail = person.getEmail();
                ownerPhone = person.getPhone();
                ownerPinfl = person.getPinfl();
                ownerResident = person.getResidentId() != null ? Long.valueOf(person.getResidentId()) : null;
            } else { 
                ownerEmail = company.getEmail();
                ownerPhone = company.getPhone();
                ownerRegion = company.getRegionId();
                ownerDistrict = company.getDistrictId();
                ownerResident = company.getResidentId();
            }

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withCatalogName("FOR_INBOUND_TRAVEL")
                    .withFunctionName("CREATE_INBOUND_CONTRACT_API")
                    .declareParameters(
                            new SqlParameter("p_user_id", Types.NUMERIC),
                            new SqlParameter("p_program_id", Types.NUMERIC),
                            new SqlParameter("p_days", Types.NUMERIC),
                            new SqlParameter("p_activity_id", Types.NUMERIC),
                            new SqlParameter("p_start_date", Types.DATE),
                            new SqlParameter("p_fizyur", Types.NUMERIC),
                            new SqlParameter("p_owner_pinfl", Types.VARCHAR), 

                            // Owner params
                            new SqlParameter("p_owner_name", Types.VARCHAR),
                            new SqlParameter("p_owner_surname", Types.VARCHAR),
                            new SqlParameter("p_owner_middle", Types.VARCHAR),
                            new SqlParameter("p_owner_birth", Types.DATE),
                            new SqlParameter("p_owner_pass_sery", Types.VARCHAR),
                            new SqlParameter("p_owner_pass_num", Types.VARCHAR),
                            new SqlParameter("p_owner_citiz", Types.NUMERIC),
                            new SqlParameter("p_owner_phone", Types.VARCHAR),
                            new SqlParameter("p_owner_gender", Types.NUMERIC),
                            new SqlParameter("p_owner_resident", Types.NUMERIC),
                            new SqlParameter("p_owner_email", Types.VARCHAR),

                            // Yur params
                            new SqlParameter("p_yur_inn", Types.VARCHAR),
                            new SqlParameter("p_yur_dir", Types.VARCHAR),
                            new SqlParameter("p_yur_addr", Types.VARCHAR),
                            new SqlParameter("p_yur_bank", Types.VARCHAR),
                            new SqlParameter("p_yur_account", Types.VARCHAR),
                            new SqlParameter("p_yur_region", Types.NUMERIC),
                            new SqlParameter("p_yur_district", Types.NUMERIC),

                            // Travelers Array
                            new SqlParameter("p_travelers", Types.ARRAY, "INBOUND_INSURED_TABLE"),

                            new SqlOutParameter("out_error", Types.INTEGER),
                            new SqlOutParameter("out_error_text", Types.VARCHAR)
                    );

            MapSqlParameterSource in = new MapSqlParameterSource()
                    .addValue("p_user_id", user.getTbId())
                    .addValue("p_program_id", details.getProgramId())
                    .addValue("p_days", details.getDays())
                    .addValue("p_activity_id", details.getActivityId())
                    .addValue("p_start_date", details.getStartDate())
                    .addValue("p_fizyur", fizYurType)
                    .addValue("p_owner_pinfl", ownerPinfl) 

                    // Person Params
                    .addValue("p_owner_name", person.getFirstName())
                    .addValue("p_owner_surname", person.getLastName())
                    .addValue("p_owner_middle", person.getMiddleName())
                    .addValue("p_owner_birth", person.getBirthDate() != null ? java.sql.Date.valueOf(person.getBirthDate()) : null)
                    .addValue("p_owner_pass_sery", person.getPassportSeries())
                    .addValue("p_owner_pass_num", person.getPassportNumber())
                    .addValue("p_owner_citiz", person.getCitizenshipId())
                    .addValue("p_owner_phone", ownerPhone)
                    .addValue("p_owner_gender", person.getGender())
                    .addValue("p_owner_resident", ownerResident)
                    .addValue("p_owner_email", ownerEmail)

                    // Company Params
                    .addValue("p_yur_inn", company.getInn())
                    .addValue("p_yur_dir", company.getDirector())
                    .addValue("p_yur_addr", company.getAddress())
                    .addValue("p_yur_bank", company.getBankName())
                    .addValue("p_yur_account", company.getAccount())
                    .addValue("p_yur_region", ownerRegion)
                    .addValue("p_yur_district", ownerDistrict)

                    // Travelers
                    .addValue("p_travelers", createTravelersArray(finalTravelersList));

            Map<String, Object> out = jdbcCall.execute(in);

            BigDecimal retVal = (BigDecimal) out.get("return"); 
            Number errCode = (Number) out.get("out_error");
            String errText = uz.insonline.travel.commons.util.CustomUtil.cleanOracleError((String) out.get("out_error_text"));

            if (errCode != null && errCode.intValue() == 0 && retVal != null) {
                contractId = retVal.longValue();
                response = new ContractResponse(0, "Success", contractId);
            } else {
                String msg = errText != null ? errText : "Unknown DB Error";
                throw new RuntimeException("DB Error (" + errCode + "): " + msg);
            }

        } catch (Exception e) {
            log.error("Inbound Create Error: ", e);
            org.springframework.transaction.interceptor.TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            response = new ContractResponse(-1, "Internal Error: " + e.getMessage());
        }

        logService.addLog("INBOUND_CREATE", dto, response.getResult(), response.getResult_message(), response, contractId);
        return response;
    }


    public ContractResponse activate(Long contractId) {
        ContractResponse response;
        try {
            int oplType = 2;

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withCatalogName("FOR_INBOUND_TRAVEL")
                    .withFunctionName("ACTIVATE_INBOUND_POLICY_AGENT")
                    .declareParameters(
                            new SqlParameter("p_contract_id", Types.NUMERIC),
                            new SqlParameter("p_opl_type", Types.NUMERIC),
                            new SqlOutParameter("policy_series", Types.VARCHAR),
                            new SqlOutParameter("policy_number", Types.NUMERIC),
                            new SqlOutParameter("error", Types.INTEGER),
                            new SqlOutParameter("error_text", Types.VARCHAR),
                            new SqlOutParameter("policy_uuid", Types.VARCHAR)
                    );

            MapSqlParameterSource in = new MapSqlParameterSource()
                    .addValue("p_contract_id", contractId)
                    .addValue("p_opl_type", oplType);

            Map<String, Object> out = jdbcCall.execute(in);

            Number errorNum = (Number) out.get("error");
            int result = errorNum != null ? errorNum.intValue() : -1;
            String errorText = (String) out.get("error_text");
            String series = (String) out.get("policy_series");
            Number number = (Number) out.get("policy_number");

            if (result == 0) {
                response = new ContractResponse(0, "Activated. Policy: " + series + " " + number, contractId);
            } else {
                response = new ContractResponse(result, "Activation Error: " + errorText);
            }

        } catch (Exception e) {
            log.error("Activation error: ", e);
            response = new ContractResponse(999, "System Error: " + e.getMessage());
        }
        return response;
    }

    public PolicyCreateResponse getPolicyLink(Long contractId) {
        PolicyCreateResponse response;
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();

            Optional<Policy> optionalPolicy = policyRepository.findByAnketaIdAndUserId(contractId, user.getTbId());

            if (optionalPolicy.isEmpty()) {
                throw  new PolicyNotFoundException(-2, "Policy not found or not active");
            } else {
                Policy policy = optionalPolicy.get();
                String newLink = "https://ersp.e-osgo.uz/site/export-to-pdf?oldAlso=yes&id=" + policy.getFondUid();
                response = new PolicyCreateResponse(0, "Success",
                        new PolicyCreateResponse.Policy(policy.getPolicySery(), String.valueOf(policy.getPolicyNumber())), newLink);
            }
        } catch (PolicyNotFoundException e) {
            throw e;
        }
        catch (Exception e) {
            log.error("Get link error: ", e);
            response = new PolicyCreateResponse(-1, e.getMessage());
        }

        logService.addLog("INBOUND_GET_LINK", null, response.getResult(), response.getResult_message(), response, contractId);
        return response;
    }
}
