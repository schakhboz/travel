package uz.insonline.travel.CentrumAir.jdbc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import uz.insonline.travel.CentrumAir.config.CentrumAirProperties;
import uz.insonline.travel.CentrumAir.domain.Kontragent;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Types;
import java.time.LocalDate;

/**
 * Весь прямой SQL модуля выпуска в одном месте: последовательности, вставки в учётные таблицы,
 * подтверждение полиса. Бизнес-логика живёт в сервисах и сюда не проникает.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class CentrumAirJdbcRepository {

    private final JdbcTemplate jdbcTemplate;
    private final CentrumAirProperties properties;

    public Long nextContractId() {
        return nextValue("Ins_Anketa_Seq");
    }

    public Long nextPolicyId() {
        return nextValue("Sq_Tb_Polis");
    }

    public Long nextTravelId() {
        return nextValue("INS_TRAVEL_SEQ");
    }

    public Long nextKontragentId() {
        return nextValue("INS_KONTRAGENT_SEQ");
    }

    public Long nextRiskDetailId() {
        return nextValue("SEQ_PASSENGER_RISKS");
    }

    public Long nextObjectRiskId() {
        return nextValue("INS_PSUGURTA_PO_OBYEKTAM_SEQ");
    }

    /** Временный номер полиса до присвоения номера НАПП — отрицательный, как принято в учётной системе. */
    public Long nextDraftPolicyNumber() {
        return jdbcTemplate.queryForObject("SELECT INS_POLIS_BASIS.nextval * (-1) FROM DUAL", Long.class);
    }

    public Long userDivision(Long userId) {
        try {
            Long division = jdbcTemplate.queryForObject("SELECT getuserdiv(?) FROM DUAL", Long.class, userId);
            return division != null && division != 0 ? division : properties.getDefaultDivisionId();
        } catch (Exception e) {
            log.warn("getuserdiv failed for user {}, falling back to default division", userId, e);
            return properties.getDefaultDivisionId();
        }
    }

    public Long insertKontragent(Kontragent kontragent, Long userId) {
        Long kontragentId = nextKontragentId();
        jdbcTemplate.update(
                "INSERT INTO INS_KONTRAGENT (TB_ID, TB_MASTERID, TB_FIZYUR, TB_NAME, TB_SURNAME, TB_PATRONYM, " +
                        "TB_PASPNUMBER, TB_PASPSERY, TB_SEX, TB_DATEBIRTH, TB_PHONE1, USER_ID, MOD_USER, TB_EMAIL, TB_ULICA, " +
                        "TB_INPS, TB_REZIDENT, TB_COUNTRY, TB_OBLAST, TB_RAYON) " +
                        "VALUES (?, ?, 0, ?, ?, ?, ?, ?, NVL(?, 0), ?, ?, ?, ?, ?, ?, ?, NVL(?, 1), NVL(?, 210), 10, 1001)",
                kontragentId, kontragentId, kontragent.firstName(), kontragent.lastName(), kontragent.middleName(),
                kontragent.passportNumber(), kontragent.passportSeries(), kontragent.gender(), kontragent.birthDate(),
                kontragent.phone(), userId, userId, kontragent.email(), kontragent.address(),
                kontragent.pinfl(), kontragent.residentType(), kontragent.citizenshipId()
        );
        return kontragentId;
    }

    public Long insertContract(Long ownerId, Long userId, Long divisionId, LocalDate startDate, LocalDate endDate,
                               BigDecimal premium, BigDecimal liability, long daysCount) {
        Long contractId = nextContractId();
        jdbcTemplate.update(
                "INSERT INTO INS_ANKETA (INS_ID, INS_TYPE, OWNER, INS_OTV, INS_PREM, " +
                        "INS_DATEF, INS_DATET, INS_DAY, USER_ID, INS_DIV, " +
                        "VAL_TYPE, VAL_KURS, VAL_USLOVIYA, INS_DATE, FIZYUR, INS_DOGNUM, INS_OTV_SUM, BENEFICIARY) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, 1, 2, SYSDATE, 0, ?, ?, ?)",
                contractId, properties.getProductId(), ownerId, liability, premium,
                startDate, endDate, daysCount, userId, divisionId,
                contractId, liability, ownerId
        );
        return contractId;
    }

    public void insertContractGroup(int policyGroup, Long contractId) {
        jdbcTemplate.update(
                "INSERT INTO INS_CENTRUM_AIR_CONTRACT_GROUP (GROUP_ID, CONTRACT_ID, CREATED_DATE) VALUES (?, ?, SYSDATE)",
                policyGroup, contractId
        );
    }

    public Long insertTravel(Long contractId, Long clientId, LocalDate birthDate, String firstName, String lastName,
                             String middleName, String passportSeries, String passportNumber, String pinfl) {
        Long travelId = nextTravelId();
        jdbcTemplate.update(
                "INSERT INTO INS_TRAVEL (INS_ID, ANKETA_ID, CLIENT_ID, YEARS, KOEF, PREM, " +
                        "FIRST_NAME, SURNAME, PATRONYM, PASPSERY, PASPNUMBER, DATEBIRTH, PINFL) " +
                        "VALUES (?, ?, ?, TRUNC(MONTHS_BETWEEN(SYSDATE, ?)/12), 1, ?, ?, ?, ?, ?, ?, ?, ?)",
                travelId, contractId, clientId, birthDate,
                BigDecimal.ZERO, // премия проставляется после разнесения по полису
                firstName, lastName, middleName, passportSeries, passportNumber, birthDate, pinfl
        );
        return travelId;
    }

    public void insertFlightPassenger(Long contractId, String flightNumber, LocalDate flightDate, Long clientId) {
        jdbcTemplate.update(
                "INSERT INTO INS_PASSENGERS (INS_ID, CONTRACT_ID, FLIGHT_NUMBER, FLIGHT_DATE, CLIENT_ID) " +
                        "VALUES (INS_PASSENGERS_SEQ.NEXTVAL, ?, ?, ?, ?)",
                contractId, flightNumber, flightDate, clientId
        );
    }

    public Long insertPolicy(Long contractId, Long userId, Long divisionId, LocalDate startDate, LocalDate endDate,
                             BigDecimal premium, BigDecimal liability) {
        Long policyId = nextPolicyId();
        jdbcTemplate.update(
                "INSERT INTO INS_POLIS (TB_ID, TB_SERY, TB_NUMBER, TB_ANKETA, TB_STATUS, " +
                        "TB_DATE_BEGIN, TB_DATE_END, TB_USER, TB_SUMMA, TB_PREMIA, " +
                        "TB_DATEPRINT, TB_DATECONTROL, TB_DIVISION, VAL_TYPE, VAL_KURS, VAL_DATE) " +
                        "VALUES (?, 'EIND', ?, ?, 1, ?, ?, ?, ?, ?, SYSDATE, SYSDATE, ?, 1, 1, SYSDATE)",
                policyId, nextDraftPolicyNumber(), contractId, startDate, endDate,
                userId, liability, premium, divisionId
        );
        return policyId;
    }

    public Long insertRiskDetail(Long contractId, Long travelId, String riskCode, BigDecimal premium) {
        Long detailId = nextRiskDetailId();
        jdbcTemplate.update(
                "INSERT INTO INS_PASSENGER_RISK_DETAILS (ID, ANKETA_ID, TRAVEL_ID, RISK_CODE, PREMIUM) " +
                        "VALUES (?, ?, ?, ?, ?)",
                detailId, contractId, travelId, riskCode, premium
        );
        return detailId;
    }

    public void insertObjectRisk(Long contractId, Long objectId, Long linkId, Long divisionId,
                                 BigDecimal premium, BigDecimal liability) {
        jdbcTemplate.update(
                "INSERT INTO INS_PSUGURTA_PO_OBYEKTAM (INS_ID, ANKETA_ID, AVTO_ID, LINK_ID, PTURI_ID, DIVISION_ID, " +
                        "VAL_TYPE, VAL_DATE, VAL_KURS, MUKOFOT_F, MUKOFOT, MAJBURIYAT, OBEKT_SONI, MUKOFOT2) " +
                        "VALUES (?, ?, ?, ?, ?, ?, 1, SYSDATE, 1, ?, ?, ?, 1, ?)",
                nextObjectRiskId(), contractId, objectId, linkId, properties.getProductId(), divisionId,
                premium, premium, liability, premium
        );
    }

    public void updateTravelPremium(Long travelId, BigDecimal premium) {
        jdbcTemplate.update("UPDATE INS_TRAVEL SET PREM = ? WHERE INS_ID = ?", premium, travelId);
    }

    public void insertPayment(Long contractId, Long policyId, BigDecimal amount, Long divisionId, Long userId,
                              String transactionId) {
        jdbcTemplate.update(
                "INSERT INTO INS_OPLATA (INS_ID, ANKETA_ID, POLIS_ID, OPL_SUMMA, OPLATA, STATUS, INS_TYPE, OPL_TYPE, " +
                        "DIVISION_ID, USER_ID, OPL_DATA, VAL_TYPE, VAL_KURS, OPL_VAL, AGENCY_TRANSACTION_ID) " +
                        "VALUES (INS_OPLATA_SEQ.NEXTVAL, ?, ?, ?, ?, 2, ?, 3, ?, ?, SYSDATE, 1, 1, 1, ?)",
                contractId, policyId, amount, amount,
                properties.getProductId(), divisionId, userId, transactionId
        );
    }

    /** Полис и договор переводятся в статус «оплачен/выпущен» после подтверждения НАПП. */
    public void markPolicyIssued(Long contractId, Long policyId) {
        jdbcTemplate.update("UPDATE INS_POLIS SET TB_STATUS = 2, TB_DATECONTROL = SYSDATE WHERE TB_ID = ?", policyId);
        jdbcTemplate.update("UPDATE INS_ANKETA SET INS_STAT = 2 WHERE INS_ID = ?", contractId);
    }

    /** Аннуляция полиса в учётной системе (процедура MAKEANNUL). */
    public int makeAnnul(Long policyId) {
        Integer result = jdbcTemplate.execute(
                (ConnectionCallback<Integer>) connection -> {
                    try (CallableStatement statement = connection.prepareCall("{call MAKEANNUL(?, ?)}")) {
                        statement.setLong(1, policyId);
                        statement.registerOutParameter(2, Types.INTEGER);
                        statement.execute();
                        return statement.getInt(2);
                    }
                });
        return result != null ? result : 0;
    }

    public void markPolicyCancelled(Long policyId, String reason, Long userId) {
        jdbcTemplate.update(
                "UPDATE INS_POLIS SET ERSP_STATUS = 1, ERSP_REASON = ?, ERSP_MOTION_USER = ? WHERE TB_ID = ?",
                reason, userId, policyId
        );
    }

    public void markCancellationFailed(Long policyId, String reason) {
        jdbcTemplate.update("UPDATE INS_POLIS SET ERSP_STATUS = 0, ERSP_REASON = ? WHERE TB_ID = ?", reason, policyId);
    }

    /** Реквизиты полиса читаются после регистрации в НАПП: серия, uuid и рег. номер приходят оттуда. */
    public IssuedPolicyRow findIssuedPolicy(Long policyId) {
        return jdbcTemplate.queryForObject(
                "SELECT TB_SERY, TB_NUMBER, FOND_UID, TB_PREMIA, TB_SUMMA FROM INS_POLIS WHERE TB_ID = ?",
                (rs, rowNum) -> new IssuedPolicyRow(
                        rs.getString("TB_SERY"),
                        rs.getLong("TB_NUMBER"),
                        rs.getString("FOND_UID"),
                        rs.getBigDecimal("TB_PREMIA"),
                        rs.getBigDecimal("TB_SUMMA")
                ),
                policyId
        );
    }

    private Long nextValue(String sequenceName) {
        return jdbcTemplate.queryForObject("SELECT " + sequenceName + ".NEXTVAL FROM DUAL", Long.class);
    }
}
