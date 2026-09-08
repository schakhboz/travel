package uz.insonline.travel.certificate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import uz.insonline.travel.certificate.api.CertificateRequest;
import uz.insonline.travel.certificate.config.CertificateProperties;
import uz.insonline.travel.certificate.model.CertificateModel;
import uz.insonline.travel.certificate.reference.InsCentrumAirPolicyRefEntity;
import uz.insonline.travel.certificate.reference.InsCentrumAirPolicyRefRepository;
import uz.insonline.travel.certificate.render.CertificatePdfRenderer;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/** Проверяет, что сертификат по максимальному набору покупки собирается и рендерится в PDF. */
@ExtendWith(MockitoExtension.class)
class CertificatePdfRendererTest {

    @Mock
    private InsCentrumAirPolicyRefRepository referenceRepository;

    private final CertificateProperties properties = content();

    @Test
    void rendersCertificateForFullPurchase() throws Exception {
        when(referenceRepository.findByRiskCode(anyString())).thenAnswer(invocation ->
                Optional.of(reference(invocation.getArgument(0))));

        CertificateModel model = new CertificateModelFactory(properties, referenceRepository)
                .build(request(), "CERT-2026-00891136");

        assertEquals(4, model.sections().size(), "ВЗР, авиа-пакет, доп. багаж и питомец — по разделу на продукт");
        assertEquals(2, model.insured().size());
        assertTrue(model.sections().get(0).attributes().stream()
                        .anyMatch(attribute -> attribute.label().equals("ОБЪЕКТ СТРАХОВАНИЯ")),
                "объект страхования берётся из справочника блоков");

        byte[] pdf = new CertificatePdfRenderer(templateEngine(), properties).render(model);

        assertTrue(pdf.length > 10_000, "PDF собран");
        assertEquals("%PDF", new String(pdf, 0, 4));
        Path out = Path.of("target", "certificate-sample.pdf");
        Files.write(out, pdf);
        System.out.println("Sample certificate: " + out.toAbsolutePath());
    }

    private static CertificateRequest request() {
        return new CertificateRequest(
                "event-1", 42L, "PNR123", "RU",
                LocalDate.of(2026, 7, 30), LocalDate.of(2026, 7, 30), LocalDate.of(2026, 8, 1),
                true, "Ivan Ivanov",
                Map.of("MAXIMUM", 0, "TRAVEL", 0, "ADDON_BAGGAGE", 4, "ANIMAL", 1),
                "MAXIMUM",
                List.of(
                        new CertificateRequest.InsuredPerson("Dale Chippi XXX", "AD-4108313",
                                LocalDate.of(1995, 8, 26), "passenger@example.com"),
                        new CertificateRequest.InsuredPerson("Pistonchi Palonchiyev", "AD-4108314",
                                LocalDate.of(1995, 8, 26), "passenger2@example.com")),
                List.of(
                        new CertificateRequest.Policy(0, "EIND", "0143857",
                                LocalDate.of(2026, 7, 30), LocalDate.of(2026, 8, 1),
                                List.of(new CertificateRequest.CoveredRisk("TRAVEL", new BigDecimal("90000"), "EUR"))),
                        new CertificateRequest.Policy(1, "EIND", "0143858",
                                LocalDate.of(2026, 7, 30), LocalDate.of(2026, 8, 1),
                                List.of(new CertificateRequest.CoveredRisk("ACCIDENT", new BigDecimal("7500"), "EUR"),
                                        new CertificateRequest.CoveredRisk("BAGGAGE", new BigDecimal("750"), "EUR"),
                                        new CertificateRequest.CoveredRisk("ADDON_BAGGAGE", new BigDecimal("750"), "EUR"),
                                        new CertificateRequest.CoveredRisk("ANIMAL", new BigDecimal("3000"), "EUR"))),
                        new CertificateRequest.Policy(2, "EIND", "0143859",
                                LocalDate.of(2026, 7, 30), LocalDate.of(2026, 8, 1),
                                List.of(new CertificateRequest.CoveredRisk("CANCEL", new BigDecimal("1000"), "EUR")))),
                false);
    }

    private static InsCentrumAirPolicyRefEntity reference(String riskCode) {
        InsCentrumAirPolicyRefEntity entity = new InsCentrumAirPolicyRefEntity();
        entity.setRiskCode(riskCode);
        entity.setProductName("Комплексное авиационное страхование");
        entity.setInsuranceRisks("Утрата, задержка более 4 часов зарегистрированного багажа");
        entity.setInsuranceObject("имущественные интересы Застрахованного лица, связанные с непредвиденными "
                + "финансовыми расходами и расходами на медицинскую помощь, во время поездки");
        entity.setTerritory("весь мир");
        return entity;
    }

    /** Читает те же тексты, что уходят в продуктив: certificate-content.yml. */
    private static CertificateProperties content() {
        try (InputStream yaml = CertificatePdfRendererTest.class.getClassLoader()
                .getResourceAsStream("certificate-content.yml")) {
            org.springframework.beans.factory.config.YamlMapFactoryBean factory =
                    new org.springframework.beans.factory.config.YamlMapFactoryBean();
            factory.setResources(new org.springframework.core.io.ByteArrayResource(yaml.readAllBytes()));
            factory.afterPropertiesSet();
            Map<String, Object> map = factory.getObject();

            org.springframework.boot.context.properties.bind.Binder binder =
                    new org.springframework.boot.context.properties.bind.Binder(
                            new org.springframework.boot.context.properties.source.MapConfigurationPropertySource(
                                    flatten("", map)));
            return binder.bind("certificate", CertificateProperties.class)
                    .orElseThrow(() -> new IllegalStateException("certificate-content.yml is not readable"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> flatten(String prefix, Map<String, Object> source) {
        Map<String, Object> flat = new java.util.LinkedHashMap<>();
        source.forEach((key, value) -> {
            String path = prefix.isEmpty() ? key : prefix + "." + key;
            if (value instanceof Map<?, ?> nested) {
                flat.putAll(flatten(path, (Map<String, Object>) nested));
            } else if (value instanceof List<?> list) {
                for (int i = 0; i < list.size(); i++) {
                    Object item = list.get(i);
                    if (item instanceof Map<?, ?> nested) {
                        flat.putAll(flatten(path + "[" + i + "]", (Map<String, Object>) nested));
                    } else {
                        flat.put(path + "[" + i + "]", item);
                    }
                }
            } else {
                flat.put(path, value);
            }
        });
        return flat;
    }

    private static TemplateEngine templateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }
}
