# CLAUDE.md

이 파일은 Claude Code가 프로젝트 작업 시 참고하는 규칙을 정의합니다.

## 사용자 정보

- Java 경험자, 현재 Kotlin 학습 중
- 클라우드 네이티브 스프링 학습 목적

## 코딩 컨벤션

### 주석 스타일
- 다중 주석은 `/** */` (KDoc/JavaDoc) 형식을 사용
- 단일 주석은 `//` 형식을 사용

```kotlin
/**
 * 다중 주석 예시
 * - 설명 1
 * - 설명 2
 */

// 단일 주석 예시
```

### 학습용 주석 작성 규칙

코드 작성 시 다음 내용을 주석으로 포함:

1. **Kotlin 학습 포인트** (Java와 비교)
   - Java와 다른 Kotlin 문법/기능 설명
   - 예: data class, null safety, 확장 함수, 스코프 함수 등
   - Java로 작성했을 때와의 차이점 비교

2. **클라우드 네이티브 스프링 학습 포인트**
   - Spring Boot 어노테이션 및 기능 설명
   - 테스트 전략 (단위 테스트, 슬라이스 테스트, 통합 테스트)
   - 클라우드 네이티브 패턴 및 모범 사례

3. **실무 관점 조언**
   - 왜 이렇게 작성하는지 이유
   - 실무에서 주의할 점
   - 성능, 유지보수성, 확장성 관점의 팁

## 프로젝트 정보

- 언어: Kotlin
- 프레임워크: Spring Boot
- 빌드 도구: Gradle
- DB 마이그레이션: Flyway

## 스키마 변경 규칙

엔티티(도메인 모델) 변경 시 반드시 Flyway 마이그레이션 파일도 함께 작성:

1. 엔티티 필드 추가/수정/삭제 시 `src/main/resources/db/migration/V{버전}__{설명}.sql` 생성
2. 버전은 기존 마이그레이션 파일의 다음 번호 사용
3. 무중단 배포를 고려하여 새 컬럼은 NULL 허용 또는 기본값 설정

```sql
-- 예시: V2__Add_publisher.sql
ALTER TABLE book ADD COLUMN publisher VARCHAR(255);
```

## 참고 문서

- `docs/15-factor-methodology.md`: 15-Factor App 방법론
  - 코드 작성 시 해당 원칙들을 고려하여 구현
  - 주석에 관련 Factor 번호와 원칙명 명시 (예: `[15-Factor #3 Config]`)
