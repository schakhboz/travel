package uz.insonline.travel.certificate;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uz.insonline.travel.certificate.config.CertificateProperties;

import java.time.LocalDate;

/** Номер сертификата вида {@code CERT-2026-00891136}: префикс, год выпуска и номер из последовательности. */
@Component
@RequiredArgsConstructor
public class CertificateNumberGenerator {

    private final JdbcTemplate jdbcTemplate;
    private final CertificateProperties properties;

    public String next(LocalDate issueDate) {
        Long sequence = jdbcTemplate.queryForObject(
                "SELECT SEQ_INS_CERTIFICATE_NUMBER.NEXTVAL FROM DUAL", Long.class);
        return "%s-%d-%08d".formatted(properties.getNumberPrefix(), issueDate.getYear(), sequence);
    }
}
