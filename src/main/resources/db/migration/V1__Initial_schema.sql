-- ===========================================
-- [클라우드 네이티브 스프링 - Flyway 마이그레이션]
-- V1__Initial_schema.sql: 초기 스키마 생성
--
-- [Flyway 파일 명명 규칙]
-- V{버전}__{설명}.sql
-- - V: 버전 마이그레이션 (순차 실행, 한 번만 실행)
-- - 버전: 숫자 (1, 2, 1.1 등)
-- - __: 언더스코어 2개 (구분자)
-- - 설명: 마이그레이션 내용 설명
--
-- [15-Factor #5 Build, Release, Run]
-- 스키마 변경을 코드와 함께 버전 관리
-- ===========================================

CREATE TABLE book (
    id                  BIGSERIAL PRIMARY KEY NOT NULL,
    isbn                VARCHAR(13) UNIQUE NOT NULL,
    title               VARCHAR(255) NOT NULL,
    author              VARCHAR(255) NOT NULL,
    price               FLOAT8 NOT NULL,
    version             INT NOT NULL,
    created_date        TIMESTAMP NOT NULL,
    last_modified_date  TIMESTAMP NOT NULL
);
