package uz.insonline.travel.commons.util;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CustomUtil {
    public static String toDate(String date) {
        if (date == null || date.isBlank())
            return null;
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate localDate = LocalDate.parse(date, inputFormatter);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return localDate.format(outputFormatter);
    }

    public static String toDate(LocalDate date, String pattern) {
        if (date == null)
            return null;
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern(pattern);
        return date.format(formatters);
    }

    public static String toDate(String date, String pattern) {
        if (date == null || date.isBlank())
            return null;
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime localDateTime = LocalDateTime.parse(date, inputFormatter);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println(localDateTime.format(outputFormatter));
        return localDateTime.format(outputFormatter);
    }

    public static String cleanOracleError(String errText) {
        if (errText == null) return null;
        if (errText.contains("ORA-")) {
            String[] parts = errText.split("ORA-\\d{5}:\\s*");
            if (parts.length > 1) {
                return parts[1].split("\n")[0].split(" ; ORA-")[0].trim();
            }
        }
        return errText;
    }
}
