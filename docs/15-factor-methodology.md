# 15-Factor App 방법론

클라우드 네이티브 애플리케이션 개발을 위한 베스트 프랙티스 가이드

## 개요

15-Factor App은 Heroku에서 제안한 12-Factor App 방법론을 Kevin Hoffman이 확장한 것입니다. 현대적인 클라우드 환경에서 확장 가능하고, 유지보수하기 쉬우며, 이식성 높은 애플리케이션을 구축하기 위한 원칙들을 정의합니다.

---

## 원래 12 Factors

### 1. Codebase (코드베이스)
> 버전 관리되는 하나의 코드베이스, 여러 배포

- 하나의 애플리케이션은 하나의 코드 저장소를 가져야 함
- 동일한 코드베이스에서 개발, 스테이징, 프로덕션 환경에 배포
- 여러 앱이 동일한 코드를 공유해야 한다면 라이브러리로 분리

```
# 좋은 예
app-repo/
├── src/
├── tests/
└── config/

# 나쁜 예: 여러 앱이 하나의 저장소에 혼재
monolith-repo/
├── app1/
├── app2/
└── shared/
```

### 2. Dependencies (의존성)
> 명시적으로 선언하고 격리

- 모든 의존성은 명시적으로 선언되어야 함
- 시스템 전역 패키지에 의존하지 않음
- 의존성 격리 도구 사용 (virtualenv, Docker 등)

```kotlin
// build.gradle.kts 예시
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
```

### 3. Config (설정)
> 환경에 설정 저장

- 설정은 코드와 분리되어야 함
- 환경 변수를 통해 설정 주입
- 설정에는 데이터베이스 URL, 외부 서비스 자격 증명, 환경별 값 등이 포함

```yaml
# application.yml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
```

### 4. Backing Services (지원 서비스)
> 연결된 리소스로 취급

- 데이터베이스, 메시지 큐, 캐시 등을 연결된 리소스로 취급
- 로컬 서비스와 서드파티 서비스를 동일하게 취급
- 설정 변경만으로 서비스 교체 가능해야 함

```
# 로컬 PostgreSQL에서 AWS RDS로 전환
DATABASE_URL=postgres://localhost/mydb
↓
DATABASE_URL=postgres://rds.amazonaws.com/mydb
```

### 5. Build, Release, Run (빌드, 릴리스, 실행)
> 빌드와 실행 단계를 엄격히 분리

| 단계 | 설명 |
|------|------|
| Build | 코드를 실행 가능한 번들로 변환 |
| Release | 빌드 결과물 + 설정 = 릴리스 |
| Run | 릴리스를 실행 환경에서 구동 |

- 각 릴리스는 고유한 ID를 가져야 함
- 릴리스는 불변(immutable)이어야 함
- 롤백은 이전 릴리스를 재배포하는 방식으로 수행

### 6. Processes (프로세스)
> 무상태(Stateless) 프로세스로 실행

- 애플리케이션은 하나 이상의 무상태 프로세스로 실행
- 영속적인 데이터는 상태 저장 백엔드 서비스에 저장
- 세션 데이터는 Redis 같은 외부 저장소에 보관

```kotlin
// 나쁜 예: 메모리에 상태 저장
val sessionCache = mutableMapOf<String, UserSession>()

// 좋은 예: 외부 저장소 사용
@Autowired
lateinit var redisTemplate: RedisTemplate<String, UserSession>
```

### 7. Port Binding (포트 바인딩)
> 포트 바인딩으로 서비스 노출

- 애플리케이션은 자체적으로 포트를 바인딩하여 서비스 제공
- 웹서버 라이브러리를 의존성으로 포함
- 다른 앱의 백엔드 서비스가 될 수 있음

```properties
# application.properties
server.port=${PORT:8080}
```

### 8. Concurrency (동시성)
> 프로세스 모델로 확장

- 수평적 확장을 통해 동시성 처리
- 프로세스 유형별로 독립적으로 확장 가능
- 워크로드 유형에 따라 다른 프로세스 타입 사용

```yaml
# Kubernetes 예시
apiVersion: apps/v1
kind: Deployment
spec:
  replicas: 3  # 수평 확장
  template:
    spec:
      containers:
      - name: catalog-service
        resources:
          requests:
            cpu: "250m"
            memory: "512Mi"
```

### 9. Disposability (폐기 가능성)
> 빠른 시작과 graceful shutdown

- 프로세스는 빠르게 시작되어야 함 (몇 초 이내)
- SIGTERM 신호를 받으면 graceful하게 종료
- 갑작스러운 종료에도 견고해야 함

```kotlin
@PreDestroy
fun cleanup() {
    // 진행 중인 작업 완료
    // 연결 정리
    logger.info("Graceful shutdown completed")
}
```

### 10. Dev/Prod Parity (개발/프로덕션 일치)
> 개발, 스테이징, 프로덕션 환경을 최대한 유사하게 유지

| 격차 | 전통적 앱 | 12-Factor 앱 |
|------|----------|-------------|
| 시간 | 몇 주 | 몇 시간 |
| 인력 | 개발자 vs 운영자 | 동일 인력 |
| 도구 | 다름 (SQLite vs PostgreSQL) | 최대한 동일 |

```yaml
# docker-compose.yml - 로컬에서도 프로덕션과 동일한 서비스 사용
services:
  postgres:
    image: postgres:15
  redis:
    image: redis:7
```

### 11. Logs (로그)
> 이벤트 스트림으로 취급

- 로그는 stdout으로 출력
- 로그 파일 관리는 애플리케이션의 책임이 아님
- 실행 환경에서 로그 수집 및 집계 처리

```kotlin
// 로그는 단순히 stdout으로 출력
private val logger = LoggerFactory.getLogger(javaClass)

fun processOrder(order: Order) {
    logger.info("Processing order: ${order.id}")
}
```

### 12. Admin Processes (관리 프로세스)
> 일회성 프로세스로 관리 작업 실행

- 데이터베이스 마이그레이션, 콘솔 실행 등
- 동일한 코드베이스와 설정 사용
- 릴리스와 함께 배포

```bash
# 마이그레이션 실행
./gradlew flywayMigrate

# 일회성 스크립트 실행
kubectl exec -it catalog-service -- ./manage.sh cleanup
```

---

## 추가된 3 Factors (Kevin Hoffman 확장)

### 13. API First (API 우선)
> API 계약을 먼저 설계

- API 스펙을 코드보다 먼저 정의
- OpenAPI/Swagger 등의 표준 사용
- 팀 간 병렬 개발 가능

```yaml
# openapi.yaml
openapi: 3.0.0
info:
  title: Catalog Service API
  version: 1.0.0
paths:
  /books:
    get:
      summary: 도서 목록 조회
      responses:
        '200':
          description: 성공
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/Book'
```

### 14. Telemetry (텔레메트리)
> 애플리케이션 성능 모니터링

세 가지 핵심 영역:

| 영역 | 설명 | 도구 예시 |
|------|------|----------|
| APM (Application Performance Monitoring) | 응답 시간, 처리량 | Prometheus, Datadog |
| Domain Metrics | 비즈니스 메트릭 | Micrometer |
| Health Checks | 서비스 상태 확인 | Spring Actuator |

```kotlin
// Spring Boot Actuator 활용
@RestController
class HealthController {

    @GetMapping("/health")
    fun health(): Map<String, String> {
        return mapOf("status" to "UP")
    }
}
```

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    tags:
      application: catalog-service
```

### 15. Authentication & Authorization (인증 및 인가)
> 보안을 처음부터 고려

- 보안은 나중에 추가하는 것이 아닌 설계 단계부터 고려
- Zero Trust 원칙 적용
- OAuth 2.0, OIDC 등 표준 프로토콜 사용

```kotlin
@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/actuator/health").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { it.jwt {} }
            .build()
    }
}
```

---

## 요약 체크리스트

| # | Factor | 핵심 질문 |
|---|--------|----------|
| 1 | Codebase | 하나의 저장소에서 여러 환경에 배포하는가? |
| 2 | Dependencies | 모든 의존성이 명시적으로 선언되어 있는가? |
| 3 | Config | 설정이 환경 변수로 관리되는가? |
| 4 | Backing Services | 외부 서비스를 연결된 리소스로 취급하는가? |
| 5 | Build, Release, Run | 빌드/릴리스/실행 단계가 분리되어 있는가? |
| 6 | Processes | 애플리케이션이 무상태인가? |
| 7 | Port Binding | 자체 포트 바인딩으로 서비스를 노출하는가? |
| 8 | Concurrency | 수평 확장이 가능한가? |
| 9 | Disposability | 빠른 시작과 graceful shutdown이 가능한가? |
| 10 | Dev/Prod Parity | 개발/프로덕션 환경이 유사한가? |
| 11 | Logs | 로그를 stdout으로 출력하는가? |
| 12 | Admin Processes | 관리 작업을 일회성 프로세스로 실행하는가? |
| 13 | API First | API 계약을 먼저 설계하는가? |
| 14 | Telemetry | 모니터링과 헬스체크가 구현되어 있는가? |
| 15 | Auth | 보안이 설계 단계부터 고려되었는가? |

---

## 참고 자료

- [The Twelve-Factor App](https://12factor.net/)
- [Beyond the Twelve-Factor App - Kevin Hoffman](https://www.oreilly.com/library/view/beyond-the-twelve-factor/9781492042631/)
- [Cloud Native Go - Kevin Hoffman](https://www.oreilly.com/library/view/cloud-native-go/9781492076322/)
