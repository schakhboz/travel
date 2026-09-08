package uz.insonline.travel.certificate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.insonline.travel.certificate.api.CertificateRequest;
import uz.insonline.travel.certificate.config.CertificateProperties;
import uz.insonline.travel.certificate.model.CertificateModel;
import uz.insonline.travel.certificate.model.CertificateSection;
import uz.insonline.travel.certificate.model.CertificateSection.Attribute;
import uz.insonline.travel.certificate.model.CertificateSection.CoverageRow;
import uz.insonline.travel.certificate.model.CertificateSection.DocumentBlock;
import uz.insonline.travel.certificate.reference.InsCentrumAirPolicyRefEntity;
import uz.insonline.travel.certificate.reference.InsCentrumAirPolicyRefRepository;
import uz.insonline.travel.certificate.render.QrCodes;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Сборка сертификата: персональные данные и суммы берутся из сообщения, объект страхования,
 * формулировки рисков и территория — из справочника блоков INS_CENTRUM_AIR_POLICY_REF (ТЗ п. 6.1),
 * покрытия и перечни документов — из конфигурации.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CertificateModelFactory {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DecimalFormat AMOUNT = new DecimalFormat("#,##0.##",
            DecimalFormatSymbols.getInstance(Locale.forLanguageTag("ru")));

    static {
        DecimalFormatSymbols symbols = AMOUNT.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        AMOUNT.setDecimalFormatSymbols(symbols);
    }

    private static final String TRAVEL = "TRAVEL";
    private static final String ACCIDENT = "ACCIDENT";
    private static final String ADDON_BAGGAGE = "ADDON_BAGGAGE";
    private static final String ANIMAL = "ANIMAL";

    private final CertificateProperties properties;
    private final InsCentrumAirPolicyRefRepository referenceRepository;

    public CertificateModel build(CertificateRequest request, String certificateNumber) {
        List<CertificateSection> sections = new ArrayList<>();
        if (hasRisk(request, TRAVEL)) {
            sections.add(travelSection(request));
        }
        if (request.packageProgram() != null) {
            sections.add(aviaSection(request));
        }
        if (quantity(request, ADDON_BAGGAGE) > 0) {
            sections.add(baggageSection(request));
        }
        if (quantity(request, ANIMAL) > 0) {
            sections.add(animalSection(request));
        }

        return new CertificateModel(
                certificateNumber,
                format(request.issueDate()),
                request.pnr(),
                request.language(),
                properties.getTemplateVersion(),
                request.insured().stream()
                        .map(person -> new CertificateModel.InsuredRow(person.fullName(), person.passport(),
                                person.birthDate() == null ? "" : person.birthDate().format(DATE)))
                        .toList(),
                sections,
                QrCodes.dataUri(properties.getBrand().getClaimUrl() + "?certificate=" + certificateNumber)
        );
    }

    private CertificateSection travelSection(CertificateRequest request) {
        CertificateProperties.Section content = section("TRAVEL");
        Optional<InsCentrumAirPolicyRefEntity> reference = referenceRepository.findByRiskCode(TRAVEL);

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("СТРАХОВЩИК", properties.getBrand().getInsurerName()));
        attributes.add(new Attribute("ТЕРРИТОРИЯ СТРАХОВАНИЯ",
                request.schengen() ? "Шенген" : territory(reference, content)));
        attributes.add(new Attribute("СРОК СТРАХОВАНИЯ", period(request, TRAVEL)));
        reference.map(InsCentrumAirPolicyRefEntity::getInsuranceObject)
                .ifPresent(object -> attributes.add(new Attribute("ОБЪЕКТ СТРАХОВАНИЯ", object)));
        attributes.add(new Attribute("НОМЕР ПОЛИСА", policyNumbers(request, TRAVEL)));

        List<CoverageRow> coverage = new ArrayList<>();
        insuredSum(request, TRAVEL).ifPresent(sum ->
                coverage.add(new CoverageRow("ОБЩАЯ СТРАХОВАЯ СУММА", sum, 0)));
        coverage.addAll(configuredCoverage(content));

        return new CertificateSection(title(content, reference, "МЕЖДУНАРОДНОЕ МЕДИЦИНСКОЕ СТРАХОВАНИЕ"),
                attributes, coverage, documents(content), true, assistance());
    }

    private CertificateSection aviaSection(CertificateRequest request) {
        String program = request.packageProgram();
        CertificateProperties.Section content = section("AVIA_" + program);
        Optional<InsCentrumAirPolicyRefEntity> reference = referenceRepository.findByRiskCode(ACCIDENT);

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("ПРОГРАММА", humanize(program)));
        attributes.add(new Attribute("ТЕРРИТОРИЯ СТРАХОВАНИЯ", territory(reference, content)));
        if (content.getTerm() != null) {
            attributes.add(new Attribute("СРОК СТРАХОВАНИЯ", content.getTerm()));
        }
        reference.map(InsCentrumAirPolicyRefEntity::getInsuranceObject)
                .ifPresent(object -> attributes.add(new Attribute("ОБЪЕКТ СТРАХОВАНИЯ", object)));
        attributes.add(new Attribute("НОМЕРА ПОЛИСОВ", aviaPolicyNumbers(request)));

        return new CertificateSection(title(content, reference, "АВИАЦИОННЫЙ ПАКЕТ"),
                attributes, configuredCoverage(content), documents(content), false, null);
    }

    private CertificateSection baggageSection(CertificateRequest request) {
        CertificateProperties.Section content = section("ADDON_BAGGAGE");
        Optional<InsCentrumAirPolicyRefEntity> reference = referenceRepository.findByRiskCode(ADDON_BAGGAGE);

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("ТЕРРИТОРИЯ СТРАХОВАНИЯ", territory(reference, content)));
        if (content.getTerm() != null) {
            attributes.add(new Attribute("СРОК СТРАХОВАНИЯ", content.getTerm()));
        }
        attributes.add(new Attribute("КОЛИЧЕСТВО ЗАСТРАХОВАННЫХ МЕСТ ДОПОЛНИТЕЛЬНОГО БАГАЖА",
                String.valueOf(quantity(request, ADDON_BAGGAGE))));
        insuredSum(request, ADDON_BAGGAGE).ifPresent(sum ->
                attributes.add(new Attribute("СТРАХОВАЯ СУММА", sum)));
        reference.map(InsCentrumAirPolicyRefEntity::getInsuranceRisks)
                .ifPresent(risks -> attributes.add(new Attribute("ЗАСТРАХОВАННЫЕ РИСКИ", risks)));
        reference.map(InsCentrumAirPolicyRefEntity::getInsuranceObject)
                .ifPresent(object -> attributes.add(new Attribute("ОБЪЕКТ СТРАХОВАНИЯ", object)));

        return new CertificateSection(title(content, reference, "СТРАХОВАНИЕ ДОПОЛНИТЕЛЬНОГО БАГАЖА"),
                attributes, configuredCoverage(content), documents(content), false, null);
    }

    private CertificateSection animalSection(CertificateRequest request) {
        CertificateProperties.Section content = section("ANIMAL");
        Optional<InsCentrumAirPolicyRefEntity> reference = referenceRepository.findByRiskCode(ANIMAL);

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("ТЕРРИТОРИЯ СТРАХОВАНИЯ", territory(reference, content)));
        if (content.getTerm() != null) {
            attributes.add(new Attribute("СРОК СТРАХОВАНИЯ", content.getTerm()));
        }
        attributes.add(new Attribute("КОЛИЧЕСТВО ЗАСТРАХОВАННЫХ ПИТОМЦЕВ",
                String.valueOf(quantity(request, ANIMAL))));
        reference.map(InsCentrumAirPolicyRefEntity::getInsuranceObject)
                .ifPresent(object -> attributes.add(new Attribute("ОБЪЕКТ СТРАХОВАНИЯ", object)));

        return new CertificateSection(title(content, reference, "СТРАХОВАНИЕ ПИТОМЦА НА ВРЕМЯ АВИАПЕРЕЛЁТА"),
                attributes, configuredCoverage(content), documents(content), false, null);
    }

    private CertificateSection.Assistance assistance() {
        CertificateProperties.Assistance contacts = properties.getAssistance();
        return new CertificateSection.Assistance(
                contacts.getName(), contacts.getSchedule(), contacts.getPhone(), contacts.getEmail(),
                contacts.getTelegram(), QrCodes.dataUri(contacts.getTelegramUrl()),
                contacts.getWhatsapp(), QrCodes.dataUri(contacts.getWhatsappUrl()),
                contacts.getInstructions(),
                properties.getBrand().getOfferUrl(), properties.getBrand().getProgramUrl());
    }

    private CertificateProperties.Section section(String key) {
        CertificateProperties.Section content = properties.getSections().get(key);
        if (content == null) {
            log.warn("Certificate content for section {} is not configured", key);
            return new CertificateProperties.Section();
        }
        return content;
    }

    private static List<CoverageRow> configuredCoverage(CertificateProperties.Section content) {
        return content.getCoverage().stream()
                .map(row -> new CoverageRow(row.getTitle(), row.getLimit(), row.getLevel()))
                .toList();
    }

    private static List<DocumentBlock> documents(CertificateProperties.Section content) {
        return content.getDocuments().stream()
                .map(block -> new DocumentBlock(block.getRisk(), block.getText()))
                .toList();
    }

    private static String title(CertificateProperties.Section content,
                               Optional<InsCentrumAirPolicyRefEntity> reference, String fallback) {
        if (content.getTitle() != null) {
            return content.getTitle();
        }
        return reference.map(InsCentrumAirPolicyRefEntity::getProductName)
                .map(String::toUpperCase)
                .orElse(fallback);
    }

    private static String territory(Optional<InsCentrumAirPolicyRefEntity> reference,
                                    CertificateProperties.Section content) {
        if (content.getTerritory() != null) {
            return content.getTerritory();
        }
        return reference.map(InsCentrumAirPolicyRefEntity::getTerritory).orElse("весь мир");
    }

    private String period(CertificateRequest request, String riskCode) {
        return policyOf(request, riskCode)
                .map(policy -> "с %s по %s".formatted(policy.startDate().format(DATE), policy.endDate().format(DATE)))
                .orElseGet(() -> "с %s по %s".formatted(
                        format(request.coverageStart()), format(request.coverageEnd())));
    }

    private static String format(LocalDate date) {
        return date == null ? "" : date.format(DATE);
    }

    private static String policyNumbers(CertificateRequest request, String riskCode) {
        return policyOf(request, riskCode)
                .map(policy -> policy.series() + " № " + policy.number())
                .orElse("");
    }

    private static String aviaPolicyNumbers(CertificateRequest request) {
        return request.policies().stream()
                .filter(policy -> policy.risks().stream().noneMatch(risk -> TRAVEL.equals(risk.code())))
                .map(policy -> policy.series() + " № " + policy.number())
                .reduce((left, right) -> left + "; " + right)
                .orElse("");
    }

    private static Optional<CertificateRequest.Policy> policyOf(CertificateRequest request, String riskCode) {
        return request.policies().stream()
                .filter(policy -> policy.risks().stream().anyMatch(risk -> riskCode.equals(risk.code())))
                .findFirst();
    }

    private static Optional<String> insuredSum(CertificateRequest request, String riskCode) {
        return request.policies().stream()
                .flatMap(policy -> policy.risks().stream())
                .filter(risk -> riskCode.equals(risk.code()) && risk.insuredSum() != null)
                .findFirst()
                .map(risk -> amount(risk.insuredSum(), risk.currency()));
    }

    private static String amount(BigDecimal value, String currency) {
        return AMOUNT.format(value) + (currency == null || currency.isBlank() ? "" : " " + currency);
    }

    private static boolean hasRisk(CertificateRequest request, String riskCode) {
        return policyOf(request, riskCode).isPresent();
    }

    private static int quantity(CertificateRequest request, String productCode) {
        return request.products() == null ? 0 : request.products().getOrDefault(productCode, 0);
    }

    private static String humanize(String program) {
        return program.charAt(0) + program.substring(1).toLowerCase();
    }
}
