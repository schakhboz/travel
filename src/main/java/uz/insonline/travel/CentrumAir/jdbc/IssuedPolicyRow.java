package uz.insonline.travel.CentrumAir.jdbc;

import java.math.BigDecimal;

/** Реквизиты полиса, присвоенные учётной системой после выпуска. */
public record IssuedPolicyRow(String series, long number, BigDecimal premiumAmount, BigDecimal liabilityAmount) {
}
