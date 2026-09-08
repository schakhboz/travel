package uz.insonline.travel.certificate.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Заявка на выпуск сертификата INSON — контракт очереди между модулем выпуска и сервисом документов.
 * <p>
 * Сообщение самодостаточно: сервис сертификатов не ходит в таблицы выпуска, ему нужен только
 * справочник блоков полей по рискам (ТЗ п. 6.1). Это позволяет вынести сервис в отдельный микросервис,
 * не меняя контракт.
 */
public record CertificateRequest(

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
