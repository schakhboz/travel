package uz.insonline.travel.CentrumAir.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.insonline.travel.CentrumAir.dto.InsurantDto;
import uz.insonline.travel.CentrumAir.dto.PassengerDto;
import uz.insonline.travel.CentrumAir.dto.ProductDto;
import uz.insonline.travel.CentrumAir.dto.SegmentDto;
import uz.insonline.travel.CentrumAir.dto.calculation.PolicyCalculationResult;
import uz.insonline.travel.CentrumAir.dto.calculation.PolicyGroupCalculation;
import uz.insonline.travel.CentrumAir.dto.request.PolicyIssueRequest;
import uz.insonline.travel.CentrumAir.dto.response.ErspPageResponse;
import uz.insonline.travel.CentrumAir.dto.response.ErspResponse;
import uz.insonline.travel.CentrumAir.entity.InsCentrumAirBookingEntity;
import uz.insonline.travel.CentrumAir.entity.InsCentrumAirPassengerEntity;
import uz.insonline.travel.CentrumAir.entity.InsCentrumAirRiskEntity;
import uz.insonline.travel.CentrumAir.entity.InsCentrumAirTariffEntity;
import uz.insonline.travel.CentrumAir.repository.InsCentrumAirBookingRepository;
import uz.insonline.travel.CentrumAir.repository.InsCentrumAirPassengerRepository;
import uz.insonline.travel.CentrumAir.repository.InsCentrumAirRiskRepository;
import uz.insonline.travel.CentrumAir.repository.InsCentrumAirTariffRepository;
import uz.insonline.travel.authentication.entity.UserEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Types;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CentrumInsuranceService {

    private final InsCentrumAirRiskRepository riskRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PolicyCalculationService calculationService;
    private final InsCentrumAirTariffRepository tariffRepository;
    private final InsCentrumAirBookingRepository bookingRepository;
    private final InsCentrumAirPassengerRepository passengerRepository;

    private static final Long PRODUCT_ID = 352L;
    private static final Set<String> PACKAGE_CODES = Set.of("STANDARD", "EXTENDED", "MAXIMUM");

    private Long saveBookingData(PolicyIssueRequest req) {
        try {
            InsCentrumAirBookingEntity booking = new InsCentrumAirBookingEntity();
            booking.setPnr(req.pnr());
            booking.setPaymentTime(req.paymentTime());
            booking.setSalesChannel(req.salesChannel());
            booking.setRouteType(req.route().routeType());
            booking.setIsInternational(req.route().isInternational());
            booking.setIsSchengen(req.route().isSchengen());
            InsCentrumAirBookingEntity saved = bookingRepository.save(booking);
            return saved.getId();
        } catch (Exception e) {
            log.error("Failed to save booking data", e);
            throw new RuntimeException("Failed to save booking", e);
        }
    }

    private void savePassengersForBooking(PolicyIssueRequest req, Long contractId, Long bookingId, List<String> riskCodes) {
        try {
            String riskCodesStr = String.join(",", riskCodes);
            for (SegmentDto segment : req.route().segments()) {
                for (PassengerDto ignored : req.passengers()) {
                    InsCentrumAirPassengerEntity passengerEntity = new InsCentrumAirPassengerEntity();
                    passengerEntity.setContractId(contractId);
                    passengerEntity.setClientId(0L);
                    passengerEntity.setBookingId(bookingId);
                    passengerEntity.setFlightNumber(segment.flightNumber());
                    passengerEntity.setDepartureAirport(segment.departureAirport());
                    passengerEntity.setArrivalAirport(segment.arrivalAirport());
                    passengerEntity.setDepartureTime(segment.departureTime());
                    passengerEntity.setArrivalTime(segment.arrivalTime());
                    passengerEntity.setSegmentOrder(segment.segmentOrder());
                    passengerEntity.setRiskCodes(riskCodesStr);
                    passengerRepository.save(passengerEntity);
                }
            }
        } catch (Exception e) {
            log.error("Failed to save passengers for contract: {}", contractId, e);
        }
    }

    @Transactional
    public ErspPageResponse<ErspResponse> issuePolicy(PolicyIssueRequest req, UserEntity user) {
        Long userId = user.getTbId();
        BigDecimal exchangeRate = BigDecimal.valueOf(13500);
        PolicyCalculationResult calcResult = calculationService.calculatePolicies(req, exchangeRate);

        Long bookingId = saveBookingData(req);
        log.info("Saved booking with ID: {}", bookingId);

        List<ErspResponse> allResponses = new ArrayList<>();
        Long divId = getUserDivision(userId);

        LocalDate startDate = req.route().segments().stream()
                .map(s -> s.departureTime().toLocalDate())
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());
        LocalDate endDate = req.route().segments().stream()
                .map(s -> s.arrivalTime().toLocalDate())
                .max(LocalDate::compareTo)
                .orElse(startDate);

        for (PolicyGroupCalculation groupCalc : calcResult.policyGroups()) {
            int groupNum = groupCalc.policyGroup();

            Long contractId = createContractForGroup(groupNum, req, userId, divId, startDate, endDate,
                    groupCalc.premiumAmount(), exchangeRate);
            List<Long> travelIds = createPassengersForContract(contractId, req, userId, startDate);
            Long policyId = createSinglePolicyForGroup(contractId, travelIds, groupCalc, req,
                    divId, startDate, endDate, userId, exchangeRate);

            String transactionId = req.transactions().isEmpty() ? null : req.transactions().get(0).transactionId();
            jdbcTemplate.update(
                    "INSERT INTO INS_OPLATA (INS_ID, ANKETA_ID, POLIS_ID, OPL_SUMMA, OPLATA, STATUS, INS_TYPE, OPL_TYPE, " +
                            "DIVISION_ID, USER_ID, OPL_DATA, VAL_TYPE, VAL_KURS, OPL_VAL, AGENCY_TRANSACTION_ID) " +
                            "VALUES (INS_OPLATA_SEQ.NEXTVAL, ?, ?, ?, ?, 2, ?, 3, ?, ?, SYSDATE, 1, 1, 1, ?)",
                    contractId, policyId, groupCalc.premiumAmount(), groupCalc.premiumAmount(),
                    PRODUCT_ID, divId, userId, transactionId
            );

            log.info("Contract: {}, Policy: {}, Premium: {}", contractId, policyId, groupCalc.premiumAmount());

            sendToErspAndConfirm(userId, contractId, policyId);

            List<String> riskCodes = getRisksForGroup(groupNum, req);
            savePassengersForBooking(req, contractId, bookingId, riskCodes);

            ErspResponse response = buildErspResponse(
                    policyId,
                    contractId,
                    groupNum,
                    startDate,
                    endDate,
                    req,
                    travelIds.size()
            );
            allResponses.add(response);
        }

        return ErspPageResponse.<ErspResponse>builder()
                .result(0)
                .resultMessage("Success")
                .page(0)
                .size(20)
                .totalElements(allResponses.size())
                .totalPages(1)
                .content(allResponses)
                .build();
    }

    private BigDecimal getTariffForRisk(String riskCode, String packageCode, boolean isRT) {
        String tariffCode;
        if ("TRAVEL".equals(riskCode)) {
            tariffCode = "TRAVEL";
        } else if ("ADDON_BAGGAGE".equals(riskCode) || "ANIMAL".equals(riskCode)) {
            tariffCode = riskCode;
        } else {
            tariffCode = packageCode;
        }

        List<InsCentrumAirTariffEntity> tariffs = tariffRepository.findByTariffCodeAndRiskRiskCode(tariffCode, riskCode);
        if (tariffs.isEmpty()) {
            throw new IllegalArgumentException("Tariff not found for risk " + riskCode + " and tariffCode " + tariffCode);
        }
        InsCentrumAirTariffEntity tariff = tariffs.get(0);
        return isRT ? tariff.getRoundTripSum() : tariff.getOneWaySum();
    }

    private Long createContractForGroup(int groupNum, PolicyIssueRequest req, Long userId, Long divId,
                                        LocalDate startDate, LocalDate endDate,
                                        BigDecimal groupPremium, BigDecimal exchangeRate) {
        Long ownerId = createInsurantKontragent(req.insurant(), userId);

        BigDecimal groupLiability = calculateGroupLiability(groupNum, req, exchangeRate);

        Long contractId = jdbcTemplate.queryForObject("SELECT Ins_Anketa_Seq.NEXTVAL FROM DUAL", Long.class);

        long daysCount = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        jdbcTemplate.update(
                "INSERT INTO INS_ANKETA (INS_ID, INS_TYPE, OWNER, INS_OTV, INS_PREM, " +
                        "INS_DATEF, INS_DATET, INS_DAY, USER_ID, INS_DIV, " +
                        "VAL_TYPE, VAL_KURS, VAL_USLOVIYA, INS_DATE, FIZYUR, INS_DOGNUM, INS_OTV_SUM, BENEFICIARY) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, 1, 2, SYSDATE, 0, ?, ?, ?)",
                contractId, PRODUCT_ID, ownerId, groupLiability, groupPremium,
                startDate, endDate, daysCount, userId, divId,
                contractId, groupLiability, ownerId
        );

        jdbcTemplate.update(
                "INSERT INTO INS_CENTRUM_AIR_CONTRACT_GROUP (GROUP_ID, CONTRACT_ID, CREATED_DATE) VALUES (?, ?, SYSDATE)",
                groupNum, contractId
        );

        return contractId;
    }

    private List<Long> createPassengersForContract(Long contractId, PolicyIssueRequest req,
                                                   Long userId, LocalDate startDate) {
        List<Long> travelIds = new ArrayList<>();
        String flightNumber = req.route().segments().isEmpty() ? null : req.route().segments().get(0).flightNumber();

        for (PassengerDto passenger : req.passengers()) {
            Long passengerClientId = createPassengerKontragent(passenger, req.insurant(), userId);
            Long travelId = jdbcTemplate.queryForObject("SELECT INS_TRAVEL_SEQ.NEXTVAL FROM DUAL", Long.class);

            jdbcTemplate.update(
                    "INSERT INTO INS_TRAVEL (INS_ID, ANKETA_ID, CLIENT_ID, YEARS, KOEF, PREM, " +
                            "FIRST_NAME, SURNAME, PATRONYM, PASPSERY, PASPNUMBER, DATEBIRTH, PINFL) " +
                            "VALUES (?, ?, ?, TRUNC(MONTHS_BETWEEN(SYSDATE, ?)/12), 1, ?, ?, ?, ?, ?, ?, ?, ?)",
                    travelId, contractId, passengerClientId, passenger.birthDate(),
                    BigDecimal.ZERO, // PREM пока 0, позже обновим через сумму полисов
                    passenger.firstName(), passenger.lastName(), passenger.middleName(),
                    passenger.passportSeries(), passenger.passportNumber(),
                    passenger.birthDate(), passenger.pinfl()
            );

            jdbcTemplate.update(
                    "INSERT INTO INS_PASSENGERS (INS_ID, CONTRACT_ID, FLIGHT_NUMBER, FLIGHT_DATE, CLIENT_ID) " +
                            "VALUES (INS_PASSENGERS_SEQ.NEXTVAL, ?, ?, ?, ?)",
                    contractId, flightNumber, startDate, passengerClientId
            );

            travelIds.add(travelId);
        }
        return travelIds;
    }

    private Long createSinglePolicyForGroup(Long contractId, List<Long> travelIds,
                                            PolicyGroupCalculation groupCalc,
                                            PolicyIssueRequest req, Long divId,
                                            LocalDate startDate, LocalDate endDate,
                                            Long userId, BigDecimal exchangeRate) {

        int groupNum = groupCalc.policyGroup();
        Long policyId = jdbcTemplate.queryForObject("SELECT Sq_Tb_Polis.Nextval FROM DUAL", Long.class);
        Long policyNumber = jdbcTemplate.queryForObject("SELECT INS_POLIS_BASIS.nextval * (-1) FROM DUAL", Long.class);

        BigDecimal groupPremium = groupCalc.premiumAmount();
        BigDecimal groupLiability = calculateGroupLiability(groupNum, req, exchangeRate);

        jdbcTemplate.update(
                "INSERT INTO INS_POLIS (TB_ID, TB_SERY, TB_NUMBER, TB_ANKETA, TB_STATUS, " +
                        "TB_DATE_BEGIN, TB_DATE_END, TB_USER, TB_SUMMA, TB_PREMIA, " +
                        "TB_DATEPRINT, TB_DATECONTROL, TB_DIVISION, VAL_TYPE, VAL_KURS, VAL_DATE) " +
                        "VALUES (?, 'EIND', ?, ?, 1, ?, ?, ?, ?, ?, SYSDATE, SYSDATE, ?, 1, 1, SYSDATE)",
                policyId, policyNumber, contractId, startDate, endDate,
                userId, groupLiability, groupPremium, divId
        );

        List<String> riskCodesForGroup = getRisksForGroup(groupNum, req);
        if (riskCodesForGroup.isEmpty()) {
            return policyId;
        }

        String packageCode = getSelectedPackage(req);
        boolean isRT = "RT".equalsIgnoreCase(req.route().routeType());
        Map<String, BigDecimal> riskTariffs = new LinkedHashMap<>();
        BigDecimal totalTariff = BigDecimal.ZERO;
        for (String riskCode : riskCodesForGroup) {
            BigDecimal tariff = getTariffForRisk(riskCode, packageCode, isRT);
            riskTariffs.put(riskCode, tariff);
            totalTariff = totalTariff.add(tariff);
        }

        for (Long travelId : travelIds) {
            for (String riskCode : riskCodesForGroup) {
                String dbRiskCode = riskCode;
                if ("ADDON_BAGGAGE".equals(riskCode)) {
                    dbRiskCode = "BAGGAGE";
                }

                Long linkId = getLinkIdForRisk(riskCode);
                boolean needsTravelId = isRiskNeedsTravelId(riskCode);

                BigDecimal riskTariff = riskTariffs.get(riskCode);
                BigDecimal riskPremium = groupPremium.multiply(riskTariff)
                        .divide(totalTariff, 2, RoundingMode.HALF_UP);

                Long detailId = jdbcTemplate.queryForObject("SELECT SEQ_PASSENGER_RISKS.NEXTVAL FROM DUAL", Long.class);
                jdbcTemplate.update(
                        "INSERT INTO INS_PASSENGER_RISK_DETAILS (ID, ANKETA_ID, TRAVEL_ID, RISK_CODE, PREMIUM) " +
                                "VALUES (?, ?, ?, ?, ?)",
                        detailId, contractId, travelId, dbRiskCode, riskPremium
                );

                Long avtoId = needsTravelId ? travelId : detailId;

                Long insId = jdbcTemplate.queryForObject("SELECT INS_PSUGURTA_PO_OBYEKTAM_SEQ.NEXTVAL FROM DUAL", Long.class);
                BigDecimal riskLiability = getRiskLiability(riskCode, exchangeRate);

                jdbcTemplate.update(
                        "INSERT INTO INS_PSUGURTA_PO_OBYEKTAM (INS_ID, ANKETA_ID, AVTO_ID, LINK_ID, PTURI_ID, DIVISION_ID, " +
                                "VAL_TYPE, VAL_DATE, VAL_KURS, MUKOFOT_F, MUKOFOT, MAJBURIYAT, OBEKT_SONI, MUKOFOT2) " +
                                "VALUES (?, ?, ?, ?, ?, ?, 1, SYSDATE, 1, ?, ?, ?, 1, ?)",
                        insId, contractId, avtoId, linkId, CentrumInsuranceService.PRODUCT_ID, divId,
                        riskPremium, riskPremium, riskLiability, riskPremium
                );
            }
        }

        BigDecimal premiumPerPassenger = groupPremium.divide(BigDecimal.valueOf(travelIds.size()), 2, RoundingMode.HALF_UP);
        for (Long travelId : travelIds) {
            jdbcTemplate.update("UPDATE INS_TRAVEL SET PREM = ? WHERE INS_ID = ?", premiumPerPassenger, travelId);
        }

        return policyId;
    }

    private Long getLinkIdForRisk(String riskCode) {
        return switch (riskCode) {
            case "TRAVEL" -> 602L;
            case "ACCIDENT" -> 592L;
            case "BAGGAGE", "ADDON_BAGGAGE", "ANIMAL" -> 603L;
            case "CANCEL", "DELAY", "DOCS" -> 593L;
            default -> throw new IllegalArgumentException("Unknown risk code: " + riskCode);
        };
    }

    private boolean isRiskNeedsTravelId(String riskCode) {
        return "TRAVEL".equals(riskCode) || "ACCIDENT".equals(riskCode);
    }

    private List<String> getRisksForGroup(int groupNum, PolicyIssueRequest req) {
        String packageCode = getSelectedPackage(req);
        switch (groupNum) {
            case 0:
                return hasProduct(req) ? List.of("TRAVEL") : Collections.emptyList();
            case 1:
                List<String> risksP1 = new ArrayList<>();
                risksP1.add("ACCIDENT");
                if ("EXTENDED".equals(packageCode) || "MAXIMUM".equals(packageCode)) {
                    risksP1.add("BAGGAGE");
                }
                int baggageQty = getProductQuantity(req, "ADDON_BAGGAGE");
                if (baggageQty > 0) risksP1.add("ADDON_BAGGAGE");
                int animalQty = getProductQuantity(req, "ANIMAL");
                if (animalQty > 0) risksP1.add("ANIMAL");
                return risksP1;
            case 2:
                return (packageCode != null) ? List.of("CANCEL") : Collections.emptyList();
            case 3:
                List<String> risksP3 = new ArrayList<>();
                risksP3.add("DOCS");
                if ("MAXIMUM".equals(packageCode)) risksP3.add("DELAY");
                return risksP3;
            default:
                return Collections.emptyList();
        }
    }

    private BigDecimal calculateGroupLiability(int groupNum, PolicyIssueRequest req, BigDecimal exchangeRate) {
        List<String> riskCodes = getRisksForGroup(groupNum, req);
        BigDecimal total = BigDecimal.ZERO;
        for (String riskCode : riskCodes) {
            List<InsCentrumAirRiskEntity> risks = riskRepository.findAllByRiskCode(riskCode);
            for (InsCentrumAirRiskEntity risk : risks) {
                BigDecimal sum = risk.getInsuranceSum();
                if ("EUR".equalsIgnoreCase(risk.getCurrency())) {
                    sum = sum.multiply(exchangeRate);
                }
                total = total.add(sum);
            }
        }
        return total.multiply(BigDecimal.valueOf(req.passengers().size()));
    }

    private BigDecimal getRiskLiability(String riskCode, BigDecimal exchangeRate) {
        List<InsCentrumAirRiskEntity> risks = riskRepository.findAllByRiskCode(riskCode);
        BigDecimal sum = BigDecimal.ZERO;
        for (InsCentrumAirRiskEntity risk : risks) {
            BigDecimal s = risk.getInsuranceSum();
            if ("EUR".equalsIgnoreCase(risk.getCurrency())) {
                s = s.multiply(exchangeRate);
            }
            sum = sum.add(s);
        }
        return sum;
    }

    private String getSelectedPackage(PolicyIssueRequest req) {
        if (req.products() == null) return null;
        return req.products().stream()
                .map(ProductDto::productCode)
                .filter(PACKAGE_CODES::contains)
                .findFirst()
                .orElse(null);
    }

    private boolean hasProduct(PolicyIssueRequest req) {
        if (req.products() == null) return false;
        return req.products().stream().anyMatch(p -> p.productCode().equalsIgnoreCase("TRAVEL"));
    }

    private int getProductQuantity(PolicyIssueRequest req, String productCode) {
        if (req.products() == null) return 0;
        return req.products().stream()
                .filter(p -> p.productCode().equalsIgnoreCase(productCode))
                .mapToInt(p -> p.quantity() != null ? p.quantity() : 0)
                .findFirst()
                .orElse(0);
    }

    public void sendToErspAndConfirm(Long userId, Long contractId, Long policyId) {
        executeErspCall("CONTRACT_TEST", userId, contractId, policyId);
        executeErspCall("CONFIRM_PAYED_TEST", userId, contractId, policyId);
        jdbcTemplate.update("UPDATE INS_POLIS SET TB_STATUS = 2, TB_DATECONTROL = SYSDATE WHERE TB_ID = ?", policyId);
        jdbcTemplate.update("UPDATE INS_ANKETA SET INS_STAT = 2 WHERE INS_ID = ?", contractId);
    }

    private void executeErspCall(String functionName, Long userId, Long contractId, Long policyId) {
        executeErspCall(functionName, userId, contractId, policyId, null);
    }

    private void executeErspCall(String functionName, Long userId, Long contractId, Long policyId, Long reqId) {
        String reqParamName = "CONTRACT_TEST".equalsIgnoreCase(functionName) ? "req_id" : "p_req_id";
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("ERSP_VOLUNTARY_INTEGRATIONS")
                .withFunctionName(functionName)
                .declareParameters(
                        new SqlOutParameter("return", Types.VARCHAR),
                        new SqlParameter("p_user_id", Types.NUMERIC),
                        new SqlParameter("p_contract_id", Types.NUMERIC),
                        new SqlParameter("p_policy_id", Types.NUMERIC),
                        new SqlParameter(reqParamName, Types.NUMERIC),
                        new SqlOutParameter("p_error_code", Types.NUMERIC),
                        new SqlOutParameter("p_error_message", Types.VARCHAR)
                );
        Map<String, Object> params = new HashMap<>();
        params.put("p_user_id", userId);
        params.put("p_contract_id", contractId);
        params.put("p_policy_id", policyId);
        params.put(reqParamName, reqId != null ? reqId : 0L);
        Map<String, Object> result = jdbcCall.execute(params);
        Number errCode = (Number) result.get("p_error_code");
        if (errCode != null && errCode.intValue() != 0) {
            throw new RuntimeException("ERSP " + functionName + " error: " + result.get("p_error_message"));
        }
    }

    private Long createInsurantKontragent(InsurantDto ins, Long userId) {
        Long kontId = jdbcTemplate.queryForObject("SELECT INS_KONTRAGENT_SEQ.NEXTVAL FROM DUAL", Long.class);
        jdbcTemplate.update(
                "INSERT INTO INS_KONTRAGENT (TB_ID, TB_MASTERID, TB_FIZYUR, TB_NAME, TB_SURNAME, TB_PATRONYM, " +
                        "TB_PASPNUMBER, TB_PASPSERY, TB_SEX, TB_DATEBIRTH, TB_PHONE1, USER_ID, MOD_USER, TB_EMAIL, TB_ULICA, " +
                        "TB_INPS, TB_REZIDENT, TB_COUNTRY, TB_OBLAST, TB_RAYON) " +
                        "VALUES (?, ?, 0, ?, ?, ?, ?, ?, NVL(?, 0), ?, ?, ?, ?, ?, ?, ?, NVL(?, 1), NVL(?, 210), 10, 1001)",
                kontId, kontId, ins.firstName(), ins.lastName(), ins.middleName(),
                ins.passportNumber(), ins.passportSeries(), ins.gender(), ins.birthDate(),
                ins.phone(), userId, userId, ins.email(), ins.address(),
                ins.pinfl(), ins.residentType(), ins.citizenshipId()
        );
        return kontId;
    }

    private Long createPassengerKontragent(PassengerDto pass, InsurantDto fallbackInsurant, Long userId) {
        Long kontId = jdbcTemplate.queryForObject("SELECT INS_KONTRAGENT_SEQ.NEXTVAL FROM DUAL", Long.class);
        jdbcTemplate.update(
                "INSERT INTO INS_KONTRAGENT (TB_ID, TB_MASTERID, TB_FIZYUR, TB_NAME, TB_SURNAME, TB_PATRONYM, " +
                        "TB_PASPNUMBER, TB_PASPSERY, TB_SEX, TB_DATEBIRTH, TB_PHONE1, USER_ID, MOD_USER, TB_EMAIL, " +
                        "TB_INPS, TB_REZIDENT, TB_COUNTRY, TB_OBLAST, TB_RAYON) " +
                        "VALUES (?, ?, 0, ?, ?, ?, ?, ?, NVL(?, 0), ?, ?, ?, ?, ?, ?, NVL(?, 1), NVL(?, 210), 10, 1001)",
                kontId, kontId, pass.firstName(), pass.lastName(), pass.middleName(),
                pass.passportNumber(), pass.passportSeries(), pass.gender(), pass.birthDate(),
                Optional.ofNullable(pass.phone()).orElse(fallbackInsurant.phone()), userId, userId,
                Optional.ofNullable(pass.email()).orElse(fallbackInsurant.email()),
                pass.pinfl(), pass.residentType(),
                Optional.ofNullable(pass.citizenshipId()).orElse(fallbackInsurant.citizenshipId())
        );
        return kontId;
    }

    private Long getUserDivision(Long userId) {
        try {
            Long div = jdbcTemplate.queryForObject("SELECT getuserdiv(?) FROM DUAL", Long.class, userId);
            return div != 0 ? div : 90000L;
        } catch (Exception e) {
            return 90000L;
        }
    }

    private ErspResponse buildErspResponse(Long policyId, Long contractId, int groupNum,
                                           LocalDate startDate, LocalDate endDate,
                                           PolicyIssueRequest req, int passengerCount) {
        return jdbcTemplate.queryForObject(
                """
                SELECT\s
                    p.TB_SERY AS policy_series,
                    p.TB_NUMBER AS policy_number,
                    p.TB_PREMIA AS premium_amount,
                    p.TB_SUMMA AS liability_amount
                FROM INS_POLIS p
                WHERE p.TB_ID = ?
               \s""",
                (rs, rowNum) -> ErspResponse.builder()
                        .policyType("AIR_TRAVEL")
                        .policyId(policyId)
                        .policySeries(rs.getString("policy_series"))
                        .policyNumber(rs.getLong("policy_number"))
                        .policyUuid(String.valueOf(rs.getLong("policy_number")))
                        .premiumAmount(rs.getBigDecimal("premium_amount"))
                        .liabilityAmount(rs.getBigDecimal("liability_amount"))
                        .riskCodes(String.join(",", getRisksForGroup(groupNum, req)))
                        .objectCount(passengerCount)
                        .contractId(contractId)
                        .policyGroup(groupNum)
                        .startDate(startDate.toString())
                        .endDate(endDate.toString())
                        .build(),
                policyId
        );
    }
}