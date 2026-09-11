package uz.insonline.travel.CentrumAir.jdbc;

import java.math.BigDecimal;

/**
 * Реквизиты полиса после выпуска. Серию, регистрационный номер и uuid присваивает НАПП:
 * пакет ERSP_VOLUNTARY_INTEGRATIONS кладёт их в INS_POLIS (TB_SERY, FOND_UID, FOND_REGISTER_NUMBER).
 */
public record IssuedPolicyRow(String series, long number, String uuid,
                              BigDecimal premiumAmount, BigDecimal liabilityAmount) {
}
