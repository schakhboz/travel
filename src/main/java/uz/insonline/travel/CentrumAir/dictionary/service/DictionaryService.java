package uz.insonline.travel.CentrumAir.dictionary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.insonline.travel.CentrumAir.dictionary.dto.RiskDto;
import uz.insonline.travel.CentrumAir.dictionary.dto.TariffDto;
import uz.insonline.travel.CentrumAir.entity.InsCentrumAirRiskEntity;
import uz.insonline.travel.CentrumAir.entity.InsCentrumAirTariffEntity;
import uz.insonline.travel.CentrumAir.repository.InsCentrumAirRiskRepository;
import uz.insonline.travel.CentrumAir.repository.InsCentrumAirTariffRepository;

import java.util.List;

/** Справочники тарифов и рисков — то, из чего калькулятор считает премию. */
@Service
@RequiredArgsConstructor
public class DictionaryService {

    private final InsCentrumAirTariffRepository tariffRepository;
    private final InsCentrumAirRiskRepository riskRepository;

    @Transactional(readOnly = true)
    public List<TariffDto> tariffs(Integer policyGroup, String tariffCode) {
        return tariffRepository.findForDictionary(policyGroup, normalize(tariffCode)).stream()
                .map(DictionaryService::toTariff)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RiskDto> risks(String riskCode) {
        List<InsCentrumAirRiskEntity> risks = riskCode == null || riskCode.isBlank()
                ? riskRepository.findAllByOrderByRiskCodeAsc()
                : riskRepository.findAllByRiskCode(normalize(riskCode));
        return risks.stream().map(DictionaryService::toRisk).toList();
    }

    private static TariffDto toTariff(InsCentrumAirTariffEntity tariff) {
        InsCentrumAirRiskEntity risk = tariff.getRisk();
        return new TariffDto(
                tariff.getId(),
                tariff.getPolicyGroup(),
                tariff.getTariffCode(),
                risk == null ? null : risk.getRiskCode(),
                risk == null ? null : risk.getTitle(),
                tariff.getOneWaySum(),
                tariff.getRoundTripSum(),
                tariff.getCurrency()
        );
    }

    private static RiskDto toRisk(InsCentrumAirRiskEntity risk) {
        return new RiskDto(risk.getId(), risk.getRiskCode(), risk.getTitle(),
                risk.getClassId(), risk.getInsuranceSum(), risk.getCurrency());
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase();
    }
}
