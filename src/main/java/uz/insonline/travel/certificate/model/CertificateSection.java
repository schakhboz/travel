package uz.insonline.travel.certificate.model;

import java.util.List;

/**
 * Раздел сертификата — один купленный продукт: ВЗР, авиа-пакет, дополнительный багаж, питомец.
 *
 * @param attributes    строки «параметр — значение»: программа, территория, срок страхования
 * @param coverage      таблица покрытий с лимитами
 * @param documents     документы для страховой выплаты
 * @param showInsured   печатать ли в разделе таблицу застрахованных лиц и общие реквизиты
 * @param assistance    блок ассистанса (только для ВЗР)
 */
public record CertificateSection(
        String title,
        List<Attribute> attributes,
        List<CoverageRow> coverage,
        List<DocumentBlock> documents,
        boolean showInsured,
        Assistance assistance
) {

    public record Attribute(String label, String value) {
    }

    /**
     * @param level 0 — итоговая строка раздела, 1 — риск, 2 — подпункт риска
     */
    public record CoverageRow(String title, String limit, int level) {
    }

    public record DocumentBlock(String risk, String documents) {
    }

    public record Assistance(String name, String schedule, String phone, String email,
                             String telegram, String telegramQr, String whatsapp, String whatsappQr,
                             List<String> instructions, String offerUrl, String programUrl) {
    }
}
