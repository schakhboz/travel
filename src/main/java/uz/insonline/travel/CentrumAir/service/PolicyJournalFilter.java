package uz.insonline.travel.CentrumAir.service;

import uz.insonline.travel.CentrumAir.error.CentrumAirApiException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Фильтр журнала полисов (ТЗ п. 8.1: date_from, date_to, product, status).
 * Продукт и статус необязательны — без них журнал возвращается за период целиком.
 */
public record PolicyJournalFilter(LocalDate dateFrom, LocalDate dateTo, String product, PolicyStatus status) {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public static PolicyJournalFilter of(String dateFrom, String dateTo, String product, String status) {
        LocalDate from = parseDate(dateFrom, "dateFrom");
        LocalDate to = parseDate(dateTo, "dateTo");
        if (to.isBefore(from)) {
            throw CentrumAirApiException.validation("dateTo must not be earlier than dateFrom");
        }
        return new PolicyJournalFilter(from, to, blankToNull(product), parseStatus(status));
    }

    private static LocalDate parseDate(String value, String field) {
        try {
            return LocalDate.parse(value, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw CentrumAirApiException.validation("Field '" + field + "' must be a date in format yyyy.MM.dd");
        }
    }

    private static PolicyStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return PolicyStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw CentrumAirApiException.validation("Field 'status' must be one of DRAFT, ISSUED, CANCELLED");
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase();
    }
}
