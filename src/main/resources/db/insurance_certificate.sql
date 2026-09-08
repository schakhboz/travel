--------------------------------------------------------------------------------
-- Сертификаты INSON (ТЗ п. 9.2): один документ на бронь, файл лежит в MinIO.
--
-- Oracle 11g. Типы и длины повторяют то, что ожидает Hibernate 6 при
-- spring.jpa.hibernate.ddl-auto = validate:
--   Long           -> NUMBER(19)
--   String(n)      -> VARCHAR2(n CHAR)   <- посимвольная семантика, иначе кириллица
--                                           в RECIPIENTS и ERROR_MESSAGE обрежется вдвое
--   OffsetDateTime -> TIMESTAMP(6) WITH TIME ZONE
--   @Lob String    -> CLOB
--
-- Ограничения 11g, которые учтены: все имена не длиннее 30 символов,
-- нет IDENTITY-колонок и DEFAULT ON NULL (это 12c) — ключи выдаёт последовательность.
--
-- Запускать под владельцем схемы приложения (OSAGO).
--------------------------------------------------------------------------------

CREATE TABLE INS_INSURANCE_CERTIFICATE (
    ID                 NUMBER(19)                  NOT NULL,
    CERTIFICATE_NUMBER VARCHAR2(50 CHAR)           NOT NULL,
    BOOKING_ID         NUMBER(19)                  NOT NULL,
    PNR                VARCHAR2(50 CHAR)           NOT NULL,
    LANGUAGE           VARCHAR2(2 CHAR)            NOT NULL,
    OBJECT_KEY         VARCHAR2(500 CHAR),
    STATUS             VARCHAR2(20 CHAR)           NOT NULL,
    RECIPIENTS         VARCHAR2(1000 CHAR),
    TEMPLATE_VERSION   VARCHAR2(20 CHAR)           NOT NULL,
    ERROR_MESSAGE      VARCHAR2(1000 CHAR),
    CREATED_AT         TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    UPDATED_AT         TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    SENT_AT            TIMESTAMP(6) WITH TIME ZONE,
    CONSTRAINT PK_INS_CERTIFICATE PRIMARY KEY (ID),
    -- Сертификат один на бронь: повторная доставка сообщения не создаёт второй документ.
    CONSTRAINT UQ_INS_CERTIFICATE_BOOKING UNIQUE (BOOKING_ID),
    CONSTRAINT UQ_INS_CERTIFICATE_NUMBER UNIQUE (CERTIFICATE_NUMBER),
    CONSTRAINT CK_INS_CERTIFICATE_STATUS
        CHECK (STATUS IN ('PENDING', 'STORED', 'SENT', 'FAILED'))
);

COMMENT ON TABLE  INS_INSURANCE_CERTIFICATE                    IS 'Страховые сертификаты INSON, выпущенные по броням';
COMMENT ON COLUMN INS_INSURANCE_CERTIFICATE.CERTIFICATE_NUMBER IS 'Номер сертификата, например CERT-2026-00891136';
COMMENT ON COLUMN INS_INSURANCE_CERTIFICATE.BOOKING_ID         IS 'Бронь INS_CENTRUM_AIR_BOOKINGS.ID';
COMMENT ON COLUMN INS_INSURANCE_CERTIFICATE.OBJECT_KEY         IS 'Ключ PDF-файла в MinIO';
COMMENT ON COLUMN INS_INSURANCE_CERTIFICATE.STATUS             IS 'PENDING, STORED, SENT, FAILED';
COMMENT ON COLUMN INS_INSURANCE_CERTIFICATE.RECIPIENTS         IS 'Адреса, на которые отправлен сертификат';
COMMENT ON COLUMN INS_INSURANCE_CERTIFICATE.TEMPLATE_VERSION   IS 'Версия шаблона — для воспроизводимости документа';

-- Поиск незавершённых и неудачных сертификатов для мониторинга и ручного перезапуска.
CREATE INDEX IX_INS_CERTIFICATE_STATUS ON INS_INSURANCE_CERTIFICATE (STATUS);

-- Первичный ключ таблицы.
CREATE SEQUENCE SEQ_INS_INSURANCE_CERTIFICATE START WITH 1 INCREMENT BY 1 NOCACHE;

-- Номер сертификата: CERT-<год>-<8 цифр>. NOCACHE, чтобы номера не терялись при рестарте.
CREATE SEQUENCE SEQ_INS_CERTIFICATE_NUMBER START WITH 1 INCREMENT BY 1 NOCACHE;

--------------------------------------------------------------------------------
-- Проверка: должно вернуть 13 колонок и 2 последовательности.
--------------------------------------------------------------------------------
-- SELECT COLUMN_NAME, DATA_TYPE, CHAR_LENGTH, CHAR_USED, NULLABLE
--   FROM USER_TAB_COLUMNS WHERE TABLE_NAME = 'INS_INSURANCE_CERTIFICATE' ORDER BY COLUMN_ID;
-- SELECT SEQUENCE_NAME FROM USER_SEQUENCES
--  WHERE SEQUENCE_NAME IN ('SEQ_INS_INSURANCE_CERTIFICATE', 'SEQ_INS_CERTIFICATE_NUMBER');

--------------------------------------------------------------------------------
-- Откат:
--------------------------------------------------------------------------------
-- DROP TABLE INS_INSURANCE_CERTIFICATE CASCADE CONSTRAINTS;
-- DROP SEQUENCE SEQ_INS_INSURANCE_CERTIFICATE;
-- DROP SEQUENCE SEQ_INS_CERTIFICATE_NUMBER;
