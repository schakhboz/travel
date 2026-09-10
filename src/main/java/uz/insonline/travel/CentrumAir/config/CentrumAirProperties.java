package uz.insonline.travel.CentrumAir.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Настройки модуля выпуска Centrum Air (ТЗ п. 11 «конфигурируемость без релиза»).
 * Значения по умолчанию повторяют константы, зашитые в коде до вынесения в конфиг.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "centrum-air")
public class CentrumAirProperties {

    /** Продукт (INS_TYPE / PTURI_ID) в учётной системе. */
    private Long productId = 352L;

    /** Подразделение, если getuserdiv() не вернул значение. */
    private Long defaultDivisionId = 90000L;

    /** Курс EUR → UZS для рисков с валютой EUR. */
    private BigDecimal eurRate = BigDecimal.valueOf(13500);

    private Ersp ersp = new Ersp();

    private Idempotency idempotency = new Idempotency();

    private Validation validation = new Validation();

    /** Проверки запроса на выпуск (ТЗ п. 7.6). */
    @Getter
    @Setter
    public static class Validation {
        /** Правила совместимости продуктов: Travel только международный RT, питомец и доп. багаж — с авиа-пакетом. */
        private boolean strictProductRules = true;

        /**
         * Сверка премии авиакомпании с расчётом по тарифной матрице (ТЗ п. 7.6.4):
         * заявленная сумма не может быть выше расчёта INSON.
         */
        private boolean checkPremium = true;

        /** Допуск на округление, UZS. По умолчанию 0 — сравнение строгое. */
        private BigDecimal premiumTolerance = BigDecimal.ZERO;
    }

    /** Идемпотентность выпуска (ТЗ п. 7.3). */
    @Getter
    @Setter
    public static class Idempotency {
        /**
         * Сколько заявка может считаться «выпускается прямо сейчас». По истечении срока
         * повторный запрос с тем же ключом перехватывает незавершённый выпуск (например, после падения узла).
         */
        private Duration inProgressTimeout = Duration.ofMinutes(5);
    }

    /** Имена функций пакета интеграции с НАПП: в тестовом и продуктивном контуре они разные (ТЗ п. 8.3). */
    @Getter
    @Setter
    public static class Ersp {
        private String catalog = "ERSP_VOLUNTARY_INTEGRATIONS";
        private String issueFunction = "CONTRACT_TEST";
        private String confirmFunction = "CONFIRM_PAYED_TEST";
        private String terminationFunction = "POLICY_TERMINATION";
    }
}
