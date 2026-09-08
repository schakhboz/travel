package uz.insonline.travel.certificate.model;

import java.util.List;

/** Готовая к отрисовке модель сертификата: одна страница на страховой продукт. */
public record CertificateModel(
        String certificateNumber,
        String issueDate,
        String pnr,
        String language,
        String templateVersion,
        List<InsuredRow> insured,
        List<CertificateSection> sections,
        String claimQrDataUri
) {

    /** Застрахованное лицо в шапке сертификата. */
    public record InsuredRow(String fullName, String passport, String birthDate) {
    }
}
