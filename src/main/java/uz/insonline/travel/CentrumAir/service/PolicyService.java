package uz.insonline.travel.CentrumAir.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uz.insonline.travel.CentrumAir.dto.IssueContentDto;
import uz.insonline.travel.CentrumAir.dto.PolicyItemDto;
import uz.insonline.travel.CentrumAir.dto.request.PolicyIssueRequest;
import uz.insonline.travel.CentrumAir.dto.response.ErspResponse;
import uz.insonline.travel.CentrumAir.dto.response.PolicyIssueResponse;
import uz.insonline.travel.CentrumAir.idempotency.IdempotencyRecord;
import uz.insonline.travel.CentrumAir.idempotency.IdempotencyService;
import uz.insonline.travel.authentication.entity.UserEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Сценарий выпуска полисов: валидация → идемпотентность → выпуск → сохранение результата (ТЗ п. 7.1).
 * Транзакции держатся на уровне отдельного полиса, поэтому сбой на середине заявки
 * оставляет уже выпущенные полисы и позволяет довыпустить остальные повторным запросом.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyService {

    private final ValidationService validationService;
    private final CentrumInsuranceService centrumInsuranceService;
    private final CentrumInsuranceGetService centrumInsuranceGetService;
    private final IdempotencyService idempotencyService;

    public PolicyIssueResponse getPolicies(PolicyJournalFilter filter) {
        return centrumInsuranceGetService.getPolicies(filter);
    }

    public PolicyIssueResponse issuePolicy(PolicyIssueRequest request, String idempotencyKey) {
        validationService.validate(request);

        IdempotencyRecord record = idempotencyService.begin(request, idempotencyKey);
        if (record.isReplay()) {
            return record.replay();
        }

        try {
            List<ErspResponse> issued = centrumInsuranceService.issuePolicy(request, currentUser(), record);
            PolicyIssueResponse response = PolicyIssueResponse.success(List.of(content(request, issued)));
            idempotencyService.complete(record.id(), response);
            return response;
        } catch (RuntimeException e) {
            log.error("Issue failed for PNR {} (idempotencyKey={})", request.pnr(), record.key(), e);
            idempotencyService.fail(record.id(), e.getMessage());
            throw e;
        }
    }

    private IssueContentDto content(PolicyIssueRequest request, List<ErspResponse> issued) {
        List<PolicyItemDto> policies = issued.stream().map(PolicyService::toPolicyItem).toList();
        return new IssueContentDto(
                request.pnr(),
                LocalDate.now(),
                request.insurant().firstName() + " " + request.insurant().lastName(),
                request.totalPremiumAmount(),
                request.premiumCurrency(),
                policies
        );
    }

    private static PolicyItemDto toPolicyItem(ErspResponse ersp) {
        return new PolicyItemDto(
                ersp.getPolicyGroup(),
                ersp.getContractId(),
                ersp.getPolicyId(),
                ersp.getPolicySeries(),
                String.valueOf(ersp.getPolicyNumber()),
                ersp.getPolicyUuid(),
                PolicyStatus.ISSUED.name(),
                ersp.getPremiumAmount(),
                ersp.getLiabilityAmount(),
                Arrays.asList(ersp.getRiskCodes().split(",")),
                ersp.getStartDate(),
                ersp.getEndDate(),
                null
        );
    }

    private static UserEntity currentUser() {
        return (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
