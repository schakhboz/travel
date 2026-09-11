package uz.insonline.travel.CentrumAir.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.insonline.travel.CentrumAir.config.CentrumAirProperties;
import uz.insonline.travel.CentrumAir.domain.ProductSelection;
import uz.insonline.travel.CentrumAir.domain.RiskCatalog;
import uz.insonline.travel.CentrumAir.dto.SegmentDto;
import uz.insonline.travel.CentrumAir.dto.calculation.PolicyCalculationResult;
import uz.insonline.travel.CentrumAir.dto.calculation.PolicyGroupCalculation;
import uz.insonline.travel.CentrumAir.dto.request.PolicyIssueRequest;
import uz.insonline.travel.CentrumAir.dto.response.ErspResponse;
import uz.insonline.travel.CentrumAir.entity.InsCentrumAirBookingEntity;
import uz.insonline.travel.CentrumAir.idempotency.IdempotencyRecord;
import uz.insonline.travel.CentrumAir.idempotency.IdempotencyRecord.IssuedGroup;
import uz.insonline.travel.CentrumAir.idempotency.IdempotencyService;
import uz.insonline.travel.CentrumAir.jdbc.CentrumAirJdbcRepository;
import uz.insonline.travel.CentrumAir.jdbc.IssuedPolicyRow;
import uz.insonline.travel.CentrumAir.repository.InsCentrumAirBookingRepository;
import uz.insonline.travel.authentication.entity.UserEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Выпуск полисов по брони (ТЗ п. 7.1): расчёт состава полисов, фиксация брони и последовательный
 * выпуск каждой учётной группы. Уже выпущенные группы повторно не выпускаются — их реквизиты
 * берутся из учётной системы.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CentrumInsuranceService {

    private final PolicyCalculationService calculationService;
    private final PolicyGroupIssuer groupIssuer;
    private final IdempotencyService idempotencyService;
    private final CentrumAirJdbcRepository jdbcRepository;
    private final InsCentrumAirBookingRepository bookingRepository;
    private final CentrumAirProperties properties;

    public IssueResult issuePolicy(PolicyIssueRequest request, UserEntity user, IdempotencyRecord record) {
        BigDecimal exchangeRate = properties.getEurRate();
        ProductSelection products = ProductSelection.of(request.products());
        PolicyCalculationResult calculation = calculationService.calculatePolicies(request, exchangeRate);

        Long bookingId = bookingId(request, record);
        Long divisionId = jdbcRepository.userDivision(user.getTbId());
        List<SegmentDto> segments = request.route().segments();
        LocalDate startDate = segments.stream().map(s -> s.departureTime().toLocalDate()).min(LocalDate::compareTo)
                .orElseThrow(() -> new IllegalArgumentException("route.segments must contain at least one segment"));
        LocalDate endDate = segments.stream().map(s -> s.arrivalTime().toLocalDate()).max(LocalDate::compareTo)
                .orElse(startDate);

        List<ErspResponse> policies = new ArrayList<>();
        for (PolicyGroupCalculation groupCalculation : calculation.policyGroups()) {
            int policyGroup = groupCalculation.policyGroup();
            IssuedGroup issued = record.issuedGroups().get(policyGroup);

            if (issued != null) {
                log.info("Policy group {} is already issued (contract={}), skipping", policyGroup, issued.contractId());
            } else {
                issued = groupIssuer.issue(new GroupIssueCommand(request, products, groupCalculation,
                        user.getTbId(), divisionId, bookingId, startDate, endDate, exchangeRate));
                idempotencyService.recordIssuedGroup(record.id(), policyGroup, issued.contractId(), issued.policyId());
            }

            policies.add(describe(issued, policyGroup, products, startDate, endDate, request.passengers().size()));
        }
        return new IssueResult(bookingId, policies);
    }

    /** Бронь фиксируется один раз на заявку: повтор после сбоя переиспользует ранее сохранённую. */
    private Long bookingId(PolicyIssueRequest request, IdempotencyRecord record) {
        if (record.bookingId() != null) {
            return record.bookingId();
        }
        InsCentrumAirBookingEntity booking = new InsCentrumAirBookingEntity();
        booking.setPnr(request.pnr());
        booking.setPaymentTime(request.paymentTime());
        booking.setSalesChannel(request.salesChannel());
        booking.setRouteType(request.route().routeType());
        booking.setIsInternational(request.route().isInternational());
        booking.setIsSchengen(request.route().isSchengen());

        Long bookingId = bookingRepository.save(booking).getId();
        idempotencyService.recordBooking(record.id(), bookingId);
        log.info("Saved booking {} for PNR {}", bookingId, request.pnr());
        return bookingId;
    }

    private ErspResponse describe(IssuedGroup issued, int policyGroup, ProductSelection products,
                                  LocalDate startDate, LocalDate endDate, int passengerCount) {
        IssuedPolicyRow policy = jdbcRepository.findIssuedPolicy(issued.policyId());
        return ErspResponse.builder()
                .policyType("AIR_TRAVEL")
                .policyId(issued.policyId())
                .policySeries(policy.series())
                .policyNumber(policy.number())
                .policyUuid(policy.uuid())
                .premiumAmount(policy.premiumAmount())
                .liabilityAmount(policy.liabilityAmount())
                .riskCodes(String.join(",", RiskCatalog.risksForGroup(policyGroup, products)))
                .objectCount(passengerCount)
                .contractId(issued.contractId())
                .policyGroup(policyGroup)
                .startDate(startDate.toString())
                .endDate(endDate.toString())
                .build();
    }
}
