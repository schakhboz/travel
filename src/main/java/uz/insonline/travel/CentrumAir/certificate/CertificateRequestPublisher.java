package uz.insonline.travel.CentrumAir.certificate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import uz.insonline.travel.CentrumAir.domain.ProductSelection;
import uz.insonline.travel.CentrumAir.dto.PassengerDto;
import uz.insonline.travel.CentrumAir.dto.request.PolicyIssueRequest;
import uz.insonline.travel.CentrumAir.dto.response.ErspResponse;
import uz.insonline.travel.CentrumAir.entity.InsCentrumAirRiskEntity;
import uz.insonline.travel.CentrumAir.repository.InsCentrumAirRiskRepository;
import uz.insonline.travel.CentrumAir.service.IssueResult;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Публикация заявки на сертификат в очередь после выпуска всех полисов брони (ТЗ п. 7.1, шаг 5).
 * PDF и письмо готовит отдельный сервис — inson-certificate-service.
 * Выпуск не ждёт документов: ответ авиакомпании уходит сразу, PDF и письмо готовятся асинхронно.
 * Сбой публикации не отменяет выпущенные полисы — он только логируется.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CertificateRequestPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final InsCentrumAirRiskRepository riskRepository;
    private final CertificateQueueProperties properties;

    public void publish(PolicyIssueRequest request, IssueResult issued) {
        try {
            rabbitTemplate.convertAndSend(
                    properties.getExchange(),
                    properties.getRoutingKey(),
                    toMessage(request, issued));
            log.info("Certificate request published for booking {} (PNR {})", issued.bookingId(), request.pnr());
        } catch (Exception e) {
            log.error("Failed to publish certificate request for booking {}; policies are issued, "
                    + "the certificate has to be re-requested", issued.bookingId(), e);
        }
    }

    private CertificateRequestMessage toMessage(PolicyIssueRequest request, IssueResult issued) {
        ProductSelection products = ProductSelection.of(request.products());
        List<CertificateRequestMessage.Policy> policies = issued.policies().stream()
                .map(this::toPolicy)
                .toList();

        return new CertificateRequestMessage(
                UUID.randomUUID().toString(),
                issued.bookingId(),
                request.pnr(),
                language(request),
                LocalDate.now(),
                coverageDate(issued, true),
                coverageDate(issued, false),
                Boolean.TRUE.equals(request.route().isSchengen()),
                request.insurant().firstName() + " " + request.insurant().lastName(),
                products.quantities(),
                products.packageCode(),
                request.passengers().stream().map(CertificateRequestPublisher::toInsured).toList(),
                policies,
                false
        );
    }

    private CertificateRequestMessage.Policy toPolicy(ErspResponse policy) {
        List<CertificateRequestMessage.CoveredRisk> risks = new ArrayList<>();
        for (String riskCode : Arrays.stream(policy.getRiskCodes().split(",")).filter(code -> !code.isBlank()).toList()) {
            Optional<InsCentrumAirRiskEntity> risk = riskRepository.findByRiskCode(riskCode);
            risks.add(new CertificateRequestMessage.CoveredRisk(riskCode,
                    risk.map(InsCentrumAirRiskEntity::getInsuranceSum).orElse(null),
                    risk.map(InsCentrumAirRiskEntity::getCurrency).orElse(null)));
        }
        return new CertificateRequestMessage.Policy(
                policy.getPolicyGroup(),
                policy.getPolicySeries(),
                String.valueOf(policy.getPolicyNumber()),
                LocalDate.parse(policy.getStartDate()),
                LocalDate.parse(policy.getEndDate()),
                risks
        );
    }

    private static CertificateRequestMessage.InsuredPerson toInsured(PassengerDto passenger) {
        String fullName = String.join(" ", passenger.lastName(), passenger.firstName(),
                passenger.middleName() == null ? "" : passenger.middleName()).trim();
        return new CertificateRequestMessage.InsuredPerson(
                fullName,
                passenger.passportSeries() + "-" + passenger.passportNumber(),
                passenger.birthDate(),
                passenger.email()
        );
    }

    private static String language(PolicyIssueRequest request) {
        return request.language() == null ? "RU" : request.language().toUpperCase();
    }

    private static LocalDate coverageDate(IssueResult issued, boolean start) {
        return issued.policies().stream()
                .map(policy -> LocalDate.parse(start ? policy.getStartDate() : policy.getEndDate()))
                .reduce((left, right) -> start
                        ? (left.isBefore(right) ? left : right)
                        : (left.isAfter(right) ? left : right))
                .orElse(LocalDate.now());
    }
}
