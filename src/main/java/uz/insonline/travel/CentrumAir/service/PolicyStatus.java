package uz.insonline.travel.CentrumAir.service;

/** Статусная модель полиса (ТЗ п. 7.2) поверх статусов учётной системы. */
public enum PolicyStatus {

    /** Заявка принята, выпуск в НАПП не завершён (INS_POLIS.TB_STATUS = 1). */
    DRAFT,

    /** Полис зарегистрирован в НАПП (INS_POLIS.TB_STATUS = 2). */
    ISSUED,

    /** Полис аннулирован (INS_POLIS.ERSP_STATUS = 1). */
    CANCELLED,

    /** Статус учётной системы не отображается в модель ТЗ. */
    UNKNOWN;

    public static PolicyStatus of(Integer policyStatus, Integer erspStatus) {
        if (erspStatus != null && erspStatus == 1) {
            return CANCELLED;
        }
        if (policyStatus == null) {
            return UNKNOWN;
        }
        return switch (policyStatus) {
            case 1 -> DRAFT;
            case 2 -> ISSUED;
            default -> UNKNOWN;
        };
    }
}
