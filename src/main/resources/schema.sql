-- ===========================================
-- [클라우드 네이티브 스프링 - 스키마 관리]
-- Spring Data JDBC는 기본적으로 schema.sql을 자동 실행
-- - JPA의 ddl-auto와 달리 명시적 스키마 정의 필요
-- - 프로덕션에서는 Flyway/Liquibase 같은 마이그레이션 도구 권장
--
-- [15-Factor #5 Build, Release, Run]
-- 스키마 변경도 코드와 함께 버전 관리
-- - 릴리스마다 일관된 스키마 보장
-- - 롤백 시 이전 스키마로 복원 가능
-- ===========================================

DROP TABLE IF EXISTS book;

CREATE TABLE book (
    -- [대리키 - BIGSERIAL]
    -- PostgreSQL의 자동 증가 정수 타입
    -- - SERIAL: 4바이트 (최대 약 21억)
    -- - BIGSERIAL: 8바이트 (최대 약 922경)
    -- - 대규모 서비스에서는 BIGSERIAL 권장
    id                  BIGSERIAL PRIMARY KEY NOT NULL,

    -- [비즈니스 식별자 - ISBN]
    -- UNIQUE 제약으로 중복 방지
    -- - 자연키이지만 PK로 사용하지 않음 (대리키 사용)
    -- - 인덱스 자동 생성됨
    isbn                VARCHAR(13) UNIQUE NOT NULL,

    title               VARCHAR(255) NOT NULL,
    author              VARCHAR(255) NOT NULL,

    -- [금액 타입]
    -- FLOAT8: PostgreSQL의 8바이트 부동소수점 (= DOUBLE PRECISION)
    -- [실무 조언] 실제 금액은 NUMERIC(정밀도, 스케일) 사용 권장
    -- 예: NUMERIC(10, 2) - 소수점 이하 2자리, 총 10자리
    price               FLOAT8 NOT NULL,

    -- [낙관적 잠금 - version]
    -- Spring Data가 UPDATE 시 자동으로 증가시킴
    -- WHERE version = ? 조건으로 동시성 충돌 감지
    version             INT NOT NULL
);
