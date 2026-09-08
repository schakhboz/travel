--------------------------------------------------------------------------------
-- Идемпотентность выпуска полисов Centrum Air (ТЗ п. 7.3).
--
-- Oracle 11g. Типы и длины повторяют то, что ожидает Hibernate 6 при
-- spring.jpa.hibernate.ddl-auto = validate:
--   Long           -> NUMBER(19)
--   Integer        -> NUMBER(10)
--   String(n)      -> VARCHAR2(n CHAR)
--   OffsetDateTime -> TIMESTAMP(6) WITH TIME ZONE
--   @Lob String    -> CLOB
--
-- Имена объектов не длиннее 30 символов — предел идентификатора в 11g.
--
-- Запускать под владельцем схемы приложения (OSAGO).
--------------------------------------------------------------------------------

CREATE TABLE INS_CENTRUM_AIR_IDEMPOTENCY (
    ID                  NUMBER(19)                  NOT NULL,
    IDEMPOTENCY_KEY     VARCHAR2(255 CHAR)          NOT NULL,
    PNR                 VARCHAR2(50 CHAR)           NOT NULL,
    PRODUCT_FINGERPRINT VARCHAR2(255 CHAR)          NOT NULL,
    REQUEST_HASH        VARCHAR2(64 CHAR)           NOT NULL,
    STATUS              VARCHAR2(20 CHAR)           NOT NULL,
    BOOKING_ID          NUMBER(19),
    RESPONSE_JSON       CLOB,
    ERROR_MESSAGE       VARCHAR2(1000 CHAR),
    CREATED_AT          TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    UPDATED_AT          TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT PK_INS_CA_IDEMPOTENCY PRIMARY KEY (ID),
    CONSTRAINT UQ_INS_CA_IDEMPOTENCY_KEY UNIQUE (IDEMPOTENCY_KEY),
    CONSTRAINT CK_INS_CA_IDEMPOTENCY_STATUS
        CHECK (STATUS IN ('IN_PROGRESS', 'PARTIAL', 'COMPLETED', 'FAILED'))
);

COMMENT ON TABLE  INS_CENTRUM_AIR_IDEMPOTENCY                     IS 'Заявки на выпуск полисов и их результат';
COMMENT ON COLUMN INS_CENTRUM_AIR_IDEMPOTENCY.IDEMPOTENCY_KEY     IS 'Заголовок Idempotency-Key либо хэш от PNR, продуктов и транзакций оплаты';
COMMENT ON COLUMN INS_CENTRUM_AIR_IDEMPOTENCY.PRODUCT_FINGERPRINT IS 'Отпечаток набора продуктов — для поиска дублей по брони';
COMMENT ON COLUMN INS_CENTRUM_AIR_IDEMPOTENCY.REQUEST_HASH        IS 'Хэш тела запроса: тот же ключ с другим телом — ошибка, а не повтор';
COMMENT ON COLUMN INS_CENTRUM_AIR_IDEMPOTENCY.RESPONSE_JSON       IS 'Ответ первичного выпуска, возвращается на повторы';

-- Поиск активных дублей по брони и набору продуктов (ТЗ п. 7.6.5).
CREATE INDEX IX_INS_CA_IDEMP_PNR ON INS_CENTRUM_AIR_IDEMPOTENCY (PNR, PRODUCT_FINGERPRINT, STATUS);

CREATE SEQUENCE SEQ_INS_CA_IDEMPOTENCY START WITH 1 INCREMENT BY 1 NOCACHE;

-- Полисы, уже выпущенные в рамках заявки: повтор довыпускает только недостающие.
CREATE TABLE INS_CENTRUM_AIR_IDEMP_POLICY (
    ID             NUMBER(19)                  NOT NULL,
    IDEMPOTENCY_ID NUMBER(19)                  NOT NULL,
    POLICY_GROUP   NUMBER(10)                  NOT NULL,
    CONTRACT_ID    NUMBER(19)                  NOT NULL,
    POLICY_ID      NUMBER(19)                  NOT NULL,
    CREATED_AT     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT PK_INS_CA_IDEMP_POLICY PRIMARY KEY (ID),
    CONSTRAINT UQ_INS_CA_IDEMP_POLICY_GROUP UNIQUE (IDEMPOTENCY_ID, POLICY_GROUP),
    CONSTRAINT FK_INS_CA_IDEMP_POLICY FOREIGN KEY (IDEMPOTENCY_ID)
        REFERENCES INS_CENTRUM_AIR_IDEMPOTENCY (ID)
);

COMMENT ON TABLE INS_CENTRUM_AIR_IDEMP_POLICY IS 'Полисы, выпущенные в рамках заявки: основа довыпуска после частичного отказа';

CREATE SEQUENCE SEQ_INS_CA_IDEMP_POLICY START WITH 1 INCREMENT BY 1 NOCACHE;

--------------------------------------------------------------------------------
-- Проверка:
--------------------------------------------------------------------------------
-- SELECT TABLE_NAME FROM USER_TABLES
--  WHERE TABLE_NAME IN ('INS_CENTRUM_AIR_IDEMPOTENCY', 'INS_CENTRUM_AIR_IDEMP_POLICY');
-- SELECT SEQUENCE_NAME FROM USER_SEQUENCES
--  WHERE SEQUENCE_NAME IN ('SEQ_INS_CA_IDEMPOTENCY', 'SEQ_INS_CA_IDEMP_POLICY');

--------------------------------------------------------------------------------
-- Откат:
--------------------------------------------------------------------------------
-- DROP TABLE INS_CENTRUM_AIR_IDEMP_POLICY CASCADE CONSTRAINTS;
-- DROP TABLE INS_CENTRUM_AIR_IDEMPOTENCY CASCADE CONSTRAINTS;
-- DROP SEQUENCE SEQ_INS_CA_IDEMP_POLICY;
-- DROP SEQUENCE SEQ_INS_CA_IDEMPOTENCY;
