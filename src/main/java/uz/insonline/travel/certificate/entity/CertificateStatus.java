package uz.insonline.travel.certificate.entity;

/** Жизненный цикл документа. */
public enum CertificateStatus {

    /** Заявка принята, PDF ещё не сформирован. */
    PENDING,

    /** PDF сформирован и сохранён в объектном хранилище. */
    STORED,

    /** PDF отправлен пассажиру письмом. */
    SENT,

    /** Генерация или отправка не удалась; сообщение ушло в DLQ. */
    FAILED
}
