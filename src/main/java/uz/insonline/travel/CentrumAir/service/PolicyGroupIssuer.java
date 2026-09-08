package uz.insonline.travel.CentrumAir.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uz.insonline.travel.CentrumAir.domain.Kontragent;
import uz.insonline.travel.CentrumAir.domain.RiskCatalog;
import uz.insonline.travel.CentrumAir.dto.PassengerDto;
import uz.insonline.travel.CentrumAir.dto.SegmentDto;
import uz.insonline.travel.CentrumAir.dto.request.PolicyIssueRequest;
import uz.insonline.travel.CentrumAir.entity.InsCentrumAirPassengerEntity;
import uz.insonline.travel.CentrumAir.idempotency.IdempotencyRecord.IssuedGroup;
import uz.insonline.travel.CentrumAir.jdbc.CentrumAirJdbcRepository;
import uz.insonline.travel.CentrumAir.jdbc.ErspGateway;
import uz.insonline.travel.CentrumAir.repository.InsCentrumAirPassengerRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Выпуск одного полиса учётной группы: договор, застрахованные, полис, риски, оплата и регистрация в НАПП.
 * <p>
 * Каждая группа выпускается в собственной транзакции: сбой на одной группе не откатывает уже выпущенные,
 * и повторный запрос с тем же ключом идемпотентности довыпускает только недостающие (ТЗ п. 7.3).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyGroupIssuer {

    private final CentrumAirJdbcRepository jdbcRepository;
    private final ErspGateway erspGateway;
    private final PolicyCalculationService calculationService;
    private final RiskLiabilityCalculator liabilityCalculator;
    private final InsCentrumAirPassengerRepository passengerRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IssuedGroup issue(GroupIssueCommand command) {
        PolicyIssueRequest request = command.request();
        int passengerCount = request.passengers().size();
        BigDecimal liability = liabilityCalculator.forGroup(
                command.policyGroup(), command.products(), passengerCount, command.exchangeRate());

        Long ownerId = jdbcRepository.insertKontragent(Kontragent.insurant(request.insurant()), command.userId());
        long daysCount = ChronoUnit.DAYS.between(command.startDate(), command.endDate()) + 1;
        Long contractId = jdbcRepository.insertContract(ownerId, command.userId(), command.divisionId(),
                command.startDate(), command.endDate(), command.premium(), liability, daysCount);
        jdbcRepository.insertContractGroup(command.policyGroup(), contractId);

        List<Long> travelIds = insurePassengers(contractId, command);

        Long policyId = jdbcRepository.insertPolicy(contractId, command.userId(), command.divisionId(),
                command.startDate(), command.endDate(), command.premium(), liability);
        spreadRisks(contractId, policyId, travelIds, command);

        jdbcRepository.insertPayment(contractId, policyId, command.premium(), command.divisionId(),
                command.userId(), transactionId(request));

        erspGateway.issueAndConfirm(command.userId(), contractId, policyId);
        jdbcRepository.markPolicyIssued(contractId, policyId);

        saveBookingPassengers(command, contractId);

        log.info("Policy group {} issued: contract={}, policy={}, premium={}",
                command.policyGroup(), contractId, policyId, command.premium());
        return new IssuedGroup(contractId, policyId);
    }

    private List<Long> insurePassengers(Long contractId, GroupIssueCommand command) {
        PolicyIssueRequest request = command.request();
        List<SegmentDto> segments = request.route().segments();
        String flightNumber = segments.isEmpty() ? null : segments.get(0).flightNumber();

        List<Long> travelIds = new ArrayList<>();
        for (PassengerDto passenger : request.passengers()) {
            Long clientId = jdbcRepository.insertKontragent(
                    Kontragent.passenger(passenger, request.insurant()), command.userId());
            travelIds.add(jdbcRepository.insertTravel(contractId, clientId, passenger.birthDate(),
                    passenger.firstName(), passenger.lastName(), passenger.middleName(),
                    passenger.passportSeries(), passenger.passportNumber(), passenger.pinfl()));
            jdbcRepository.insertFlightPassenger(contractId, flightNumber, command.startDate(), clientId);
        }
        return travelIds;
    }

    /** Разносит премию полиса по рискам и застрахованным: запись риска, объект страхования, премия застрахованного. */
    private void spreadRisks(Long contractId, Long policyId, List<Long> travelIds, GroupIssueCommand command) {
        List<String> riskCodes = RiskCatalog.risksForGroup(command.policyGroup(), command.products());
        if (riskCodes.isEmpty()) {
            log.warn("Policy {} of group {} has no risks", policyId, command.policyGroup());
            return;
        }

        Map<String, BigDecimal> riskPremiums = calculationService.splitPremiumByRisk(
                command.premium(), riskCodes, command.products().packageCode(),
                PolicyCalculationService.isRoundTrip(command.request()));
        Map<String, BigDecimal> riskLiabilities = riskCodes.stream().distinct().collect(Collectors.toMap(
                riskCode -> riskCode, riskCode -> liabilityCalculator.forRisk(riskCode, command.exchangeRate())));

        for (Long travelId : travelIds) {
            for (String riskCode : riskCodes) {
                BigDecimal riskPremium = riskPremiums.get(riskCode);
                Long detailId = jdbcRepository.insertRiskDetail(contractId, travelId,
                        RiskCatalog.storageCode(riskCode), riskPremium);
                Long objectId = RiskCatalog.boundToTravel(riskCode) ? travelId : detailId;
                jdbcRepository.insertObjectRisk(contractId, objectId, RiskCatalog.linkIdFor(riskCode),
                        command.divisionId(), riskPremium, riskLiabilities.get(riskCode));
            }
        }

        BigDecimal premiumPerPassenger = command.premium()
                .divide(BigDecimal.valueOf(travelIds.size()), 2, RoundingMode.HALF_UP);
        travelIds.forEach(travelId -> jdbcRepository.updateTravelPremium(travelId, premiumPerPassenger));
    }

    /** Журнал маршрута брони: по строке на сегмент и пассажира. Сбой журнала не отменяет выпущенный полис. */
    private void saveBookingPassengers(GroupIssueCommand command, Long contractId) {
        String riskCodes = String.join(",", RiskCatalog.risksForGroup(command.policyGroup(), command.products()));
        try {
            for (SegmentDto segment : command.request().route().segments()) {
                for (PassengerDto ignored : command.request().passengers()) {
                    InsCentrumAirPassengerEntity entity = new InsCentrumAirPassengerEntity();
                    entity.setContractId(contractId);
                    entity.setClientId(0L);
                    entity.setBookingId(command.bookingId());
                    entity.setFlightNumber(segment.flightNumber());
                    entity.setDepartureAirport(segment.departureAirport());
                    entity.setArrivalAirport(segment.arrivalAirport());
                    entity.setDepartureTime(segment.departureTime());
                    entity.setArrivalTime(segment.arrivalTime());
                    entity.setSegmentOrder(segment.segmentOrder());
                    entity.setRiskCodes(riskCodes);
                    passengerRepository.save(entity);
                }
            }
        } catch (Exception e) {
            log.error("Failed to save booking passengers for contract: {}", contractId, e);
        }
    }

    private static String transactionId(PolicyIssueRequest request) {
        return request.transactions().isEmpty() ? null : request.transactions().get(0).transactionId();
    }
}
