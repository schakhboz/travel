package uz.insonline.travel.CentrumAir.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uz.insonline.travel.CentrumAir.dto.IssueContentDto;
import uz.insonline.travel.CentrumAir.dto.PolicyItemDto;
import uz.insonline.travel.CentrumAir.dto.request.PolicyIssueRequest;
import uz.insonline.travel.CentrumAir.dto.response.ErspPageResponse;
import uz.insonline.travel.CentrumAir.dto.response.ErspResponse;
import uz.insonline.travel.CentrumAir.dto.response.PolicyIssueResponse;
import uz.insonline.travel.authentication.entity.UserEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyService {

    private final ValidationService validationService;
    private final CentrumInsuranceService centrumInsuranceService;
    private final CentrumInsuranceGetService centrumInsuranceGetService;

    public PolicyIssueResponse getPolicies(String dateFrom, String dateTo) {
        return centrumInsuranceGetService.getPolicies(dateFrom, dateTo);
    }

    public PolicyIssueResponse issuePolicy(PolicyIssueRequest request) {
        validationService.validate(request);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();

        ErspPageResponse<ErspResponse> erspResponse = centrumInsuranceService.issuePolicy(request, user);

        List<PolicyItemDto> policyItems = erspResponse.getContent().stream()
                .map(ersp -> {
                    String startDateTime = ersp.getStartDate();
                    String endDateTime = ersp.getEndDate();

                    List<String> riskCodes = Arrays.asList(ersp.getRiskCodes().split(","));

                    return new PolicyItemDto(
                            ersp.getPolicyGroup(),
                            ersp.getContractId(),
                            ersp.getPolicyId(),
                            ersp.getPolicySeries(),
                            String.valueOf(ersp.getPolicyNumber()),
                            ersp.getPolicyUuid(),
                            "ISSUED",
                            ersp.getPremiumAmount(),
                            ersp.getLiabilityAmount(),
                            riskCodes,
                            startDateTime,
                            endDateTime,
                            null
                    );
                })
                .collect(Collectors.toList());

        String insurantName = request.insurant().firstName() + " " + request.insurant().lastName();
        IssueContentDto content = new IssueContentDto(
                request.pnr(),
                LocalDate.now(),
                insurantName,
                request.totalPremiumAmount(),
                request.premiumCurrency(),
                policyItems
        );

        return PolicyIssueResponse.success(Collections.singletonList(content));
    }
}