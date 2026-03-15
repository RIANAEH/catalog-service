-- ===========================================
-- [클라우드 네이티브 스프링 - Flyway 마이그레이션]
-- V3__Make_publisher_not_null.sql: publisher 컬럼 NOT NULL 제약 추가
--
-- [실무 조언 - 무중단 배포를 위한 2단계 마이그레이션]
-- 1단계: 기존 null 데이터를 기본값으로 업데이트
-- 2단계: NOT NULL 제약조건 추가
--
-- 이렇게 분리하면:
-- - 기존 데이터 손실 없음
-- - 애플리케이션 배포와 DB 마이그레이션 순서에 유연성 확보
-- ===========================================

-- 1단계: 기존 null 데이터를 기본값으로 업데이트
UPDATE book SET publisher = 'Unknown' WHERE publisher IS NULL;

-- 2단계: NOT NULL 제약조건 추가
ALTER TABLE book ALTER COLUMN publisher SET NOT NULL;
