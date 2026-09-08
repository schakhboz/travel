package uz.insonline.travel.certificate.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Содержание сертификата INSON (ТЗ п. 9.2). Тексты покрытий, сроков и перечни документов —
 * конфигурация, а не код: продукт меняет их без релиза. Персональные данные и суммы приходят
 * в сообщении, объект страхования и формулировки рисков — из справочника блоков (ТЗ п. 6.1).
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "certificate")
public class CertificateProperties {

    /** Версия шаблона фиксируется в БД для воспроизводимости документа (ТЗ п. 9.2). */
    private String templateVersion = "1.0";

    private String numberPrefix = "CERT";

    private Rabbit rabbit = new Rabbit();
    private Brand brand = new Brand();
    private Assistance assistance = new Assistance();
    private Mail mail = new Mail();

    /** Разделы: TRAVEL, AVIA_STANDARD, AVIA_EXTENDED, AVIA_MAXIMUM, ADDON_BAGGAGE, ANIMAL. */
    private Map<String, Section> sections = new LinkedHashMap<>();

    @Getter
    @Setter
    public static class Rabbit {
        private String exchange = "insurance.certificate.exchange";
        private String queue = "insurance.certificate.queue";
        private String routingKey = "insurance.certificate.issue";
        private String deadLetterQueue = "insurance.certificate.queue.dlq";
        private String deadLetterRoutingKey = "insurance.certificate.issue.dlq";
    }

    @Getter
    @Setter
    public static class Brand {
        private String insurerName = "АО СО «INSON»";
        private String callCenter = "1371";
        private String supportEmail = "info@insuranceon.uz";
        private String offerUrl = "https://insuranceon.uz/";
        private String programUrl = "https://insuranceon.uz/";
        /** Лендинг подачи заявления об убытке — на него ведёт QR-код сертификата. */
        private String claimUrl = "https://insuranceon.uz/claim";
        /** Логотип в шапке: файл в classpath. Если файла нет, печатается текстовый логотип. */
        private String logoResource = "certificate/logo.png";
    }

    @Getter
    @Setter
    public static class Assistance {
        private String name = "SBR Global Assist";
        private String schedule = "24/7 · Английский, узбекский";
        private String phone = "+90 (242) 606 10 77";
        private String email = "operation@sbrassistance.com";
        private String telegram = "+90 (541) 662 60 77";
        private String telegramUrl = "https://t.me/+905416626077";
        private String whatsapp = "+90 (541) 662 60 77";
        private String whatsappUrl = "https://wa.me/905416626077";
        private List<String> instructions = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class Mail {
        private String from = "info@insuranceon.uz";
        private String subject = "Ваш страховой полис";
        private String fileNamePattern = "INSON_CERT_%s.pdf";
    }

    @Getter
    @Setter
    public static class Section {
        private String title;
        /** Формулировка срока страхования, если она текстовая (авиа-пакет, багаж, питомец). */
        private String term;
        /** Территория, если она не берётся из справочника блоков. */
        private String territory;
        private List<Coverage> coverage = new ArrayList<>();
        private List<Documents> documents = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class Coverage {
        private String title;
        private String limit;
        /** 0 — итог раздела, 1 — риск, 2 — подпункт риска. */
        private int level = 1;
    }

    @Getter
    @Setter
    public static class Documents {
        private String risk;
        private String text;
    }
}
