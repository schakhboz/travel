package uz.insonline.travel.CentrumAir.certificate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Заявка на выпуск сертификата — контракт очереди с сервисом документов (inson-certificate-service).
 * <p>
 * Сообщение самодостаточно: получателю не нужны таблицы выпуска. Копия этого контракта живёт
 * на стороне сервиса сертификатов; обе стороны сериализуют одинаковый JSON, поэтому имя пакета
 * значения не имеет. Менять поля можно только совместимо с получателем.
 */
public record CertificateRequestMessage(

        /** Идентификатор события для дедупликации повторных доставок. */
        String eventId,

        Long bookingId,
        String pnr,

        /** Язык документа и письма: RU / UZ / EN. */
        String language,

        LocalDate issueDate,
        LocalDate coverageStart,
        LocalDate coverageEnd,

        /** Признак шенгенского маршрута — влияет на территорию страхования по ВЗР. */
        boolean schengen,

        String insurantName,

        /** Купленные продукты: код → количество (места багажа, питомцы). */
        Map<String, Integer> products,

        /** Программа авиа-пакета: STANDARD / EXTENDED / MAXIMUM, либо null. */
        String packageProgram,

        List<InsuredPerson> insured,
        List<Policy> policies,

        /** Повторная отправка по запросу оператора: PDF не перегенерируется, если уже сохранён. */
        boolean resend
) {

    public record InsuredPerson(String fullName, String passport, LocalDate birthDate, String email) {
    }

    public record Policy(Integer policyGroup, String series, String number,
                         LocalDate startDate, LocalDate endDate, List<CoveredRisk> risks) {
    }

    public record CoveredRisk(String code, BigDecimal insuredSum, String currency) {
    }
}
