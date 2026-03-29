# Dockerfile vs Cloud Native Buildpacks

Spring Boot 애플리케이션을 컨테이너 이미지로 만드는 두 가지 방법 비교

---

## 개요

컨테이너 이미지를 빌드하는 방법은 크게 두 가지가 있습니다.

1. **Dockerfile**: 개발자가 직접 이미지 빌드 단계를 정의
2. **Cloud Native Buildpacks**: 빌드팩이 소스코드를 분석해 자동으로 이미지 생성

이 프로젝트는 두 방식을 모두 경험해볼 수 있도록 구성되어 있습니다.
- `Dockerfile`: 멀티 스테이지 빌드 방식
- `build.gradle.kts`의 `bootBuildImage`: Spring Boot 플러그인 빌드팩 방식

---

## 1. Dockerfile 방식

### 개념

개발자가 `Dockerfile`에 이미지 빌드 단계를 직접 명세합니다. 어떤 베이스 이미지를 쓸지, 어떤 파일을 복사할지, 어떤 명령을 실행할지 모두 수동으로 정의합니다.

### 이 프로젝트의 Dockerfile 분석

```dockerfile
# ── 1단계: Builder ──────────────────────────────────────────
FROM eclipse-temurin:17 As builder
WORKDIR workspace
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} catalog-service.jar

# layertools: JAR를 레이어별로 분리하는 Spring Boot 내장 도구
# - dependencies      : 외부 라이브러리 (거의 변경되지 않음)
# - spring-boot-loader: 스프링 부트 로더
# - snapshot-dependencies: 스냅샷 의존성
# - application       : 실제 애플리케이션 코드 (자주 변경됨)
RUN java -Djarmode=layertools -jar catalog-service.jar extract

# ── 2단계: Runtime ──────────────────────────────────────────
FROM eclipse-temurin:17
RUN useradd spring   # 보안: 비루트 사용자 생성
USER spring
WORKDIR workspace

# 변경 빈도가 낮은 레이어를 먼저 복사 → Docker 캐시 최대 활용
COPY --from=builder workspace/dependencies/ ./
COPY --from=builder workspace/spring-boot-loader/ ./
COPY --from=builder workspace/snapshot-dependencies/ ./
COPY --from=builder workspace/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
```

### 멀티 스테이지 빌드란?

```
┌─────────────────────────────────────────────────────────────┐
│ 빌드 컨텍스트                                                │
│                                                             │
│  Stage 1: builder          Stage 2: runtime                 │
│  ┌──────────────────┐      ┌──────────────────┐            │
│  │ eclipse-temurin  │      │ eclipse-temurin  │            │
│  │                  │      │                  │            │
│  │ JAR 복사         │ ───► │ 레이어만 복사     │            │
│  │ layertools 실행  │      │ (빌드 도구 제외)  │            │
│  │ 레이어 분리      │      │                  │            │
│  └──────────────────┘      └──────────────────┘            │
│                              ↑ 최종 이미지                  │
└─────────────────────────────────────────────────────────────┘
```

**왜 멀티 스테이지인가?**
- Stage 1(builder)에는 빌드 도구, 소스코드 등 런타임에 불필요한 파일이 포함됨
- Stage 2(runtime)에는 실행에 필요한 파일만 복사 → 최종 이미지 크기 최소화
- 빌드 도구 취약점이 최종 이미지에 노출되지 않음

### Docker 레이어 캐시 전략

```
코드 변경 시 재빌드 범위:

[변경 없음] dependencies/       → 캐시 사용 ✓  (수백 MB, 느림)
[변경 없음] spring-boot-loader/ → 캐시 사용 ✓
[변경 없음] snapshot-dependencies/ → 캐시 사용 ✓
[변경됨]   application/         → 재빌드    ✗  (수 KB, 빠름)

코드만 바꿨는데 전체를 다시 빌드하면 매번 의존성을 다 받아야 함
→ 변경 빈도에 따라 레이어 순서를 배치하면 빌드 시간을 크게 단축할 수 있음
```

### 실행 방법

```bash
# 1. 먼저 JAR 빌드
./gradlew bootJar

# 2. Docker 이미지 빌드
docker build -t catalog-service:latest .

# 3. 실행
docker run --name catalog-service -p 9001:9001 catalog-service:latest
```

---

## 2. Cloud Native Buildpacks 방식

### 개념

[Cloud Native Buildpacks(CNB)](https://buildpacks.io)는 CNCF(Cloud Native Computing Foundation) 표준으로, 소스코드 또는 JAR를 분석해 자동으로 OCI 컨테이너 이미지를 만들어주는 기술입니다. Spring Boot 플러그인은 [Paketo Buildpacks](https://paketo.io)를 기본으로 사용합니다.

```
소스코드/JAR
    │
    ▼
┌──────────────────────────────────────┐
│         Buildpack 실행 흐름           │
│                                      │
│  detect   → 어떤 언어/프레임워크?      │
│  analyze  → 이전 빌드와 차이 분석     │
│  restore  → 캐시 복원                │
│  build    → 의존성 설치, 레이어 구성   │
│  export   → OCI 이미지 생성          │
└──────────────────────────────────────┘
    │
    ▼
OCI 이미지 (Docker Hub, ECR 등 어디든 push 가능)
```

### build.gradle.kts 설정

```kotlin
tasks.bootBuildImage {
    imageName.set("${project.name}:${project.version}")

    environment.set(
        mapOf(
            "BP_JVM_VERSION" to "17"   // 사용할 JVM 버전 지정
        )
    )
}
```

주요 환경 변수(BP_*):

| 변수 | 설명 | 예시 |
|------|------|------|
| `BP_JVM_VERSION` | JVM 버전 | `"17"` |
| `BP_JVM_TYPE` | JDK or JRE | `"JRE"` |
| `BPE_JAVA_TOOL_OPTIONS` | JVM 옵션 추가 | `"-XX:+UseG1GC"` |
| `BP_SPRING_CLOUD_BINDINGS_DISABLED` | Spring Cloud Bindings 비활성화 | `"true"` |

### 실행 방법

```bash
# JAR 빌드 + 이미지 빌드를 한 번에 처리
./gradlew bootBuildImage

# 실행
docker run --name catalog-service -p 9001:9001 catalog-service:0.0.1-SNAPSHOT
```

---

## 3. 상세 비교

### 빌드 프로세스

| 항목 | Dockerfile | Buildpacks |
|------|-----------|------------|
| 필요 파일 | `Dockerfile` 직접 작성 | 없음 (Gradle 설정만으로 충분) |
| 사전 작업 | `./gradlew bootJar` + `docker build` | `./gradlew bootBuildImage` 하나로 완결 |
| 빌드 도구 | Docker CLI, Docker 데몬 필요 | Docker 데몬만 필요 (CLI 불필요) |
| 이미지 형식 | Docker 이미지 | OCI 표준 이미지 |

### 보안

| 항목 | Dockerfile | Buildpacks |
|------|-----------|------------|
| 비루트 실행 | `RUN useradd spring; USER spring` 직접 작성 | CNB 사용자로 자동 실행 (기본 적용) |
| 베이스 이미지 보안 패치 | 개발자가 직접 이미지 버전 올려야 함 | 빌드팩 업데이트로 자동 반영 |
| 최소 권한 원칙 | 수동 구현 | 기본 적용 |
| OS 취약점 스캔 | 별도 도구 필요 (Trivy 등) | Paketo가 알려진 CVE 반영하여 주기적 업데이트 |

### 이미지 최적화

| 항목 | Dockerfile | Buildpacks |
|------|-----------|------------|
| 레이어 구성 | `layertools`로 수동 분리 | 자동으로 최적 레이어 구성 |
| 레이어 재사용 | Docker 캐시 활용 (순서 중요) | Buildpack 캐시 자동 관리 |
| 이미지 크기 | 멀티 스테이지로 최소화 가능 | 유사한 수준으로 자동 최적화 |

### 유지보수

| 항목 | Dockerfile | Buildpacks |
|------|-----------|------------|
| JDK 버전 변경 | `FROM eclipse-temurin:17` 수정 | `BP_JVM_VERSION` 값 변경 |
| OS 업그레이드 | 베이스 이미지 직접 교체 | `./gradlew bootBuildImage` 재실행 |
| 멀티 아키텍처 지원 | `--platform` 플래그 + 별도 설정 | `--platform` 옵션으로 지원 가능 |
| 학습 비용 | Dockerfile 문법, 레이어 개념 이해 필요 | Gradle 설정만 알면 됨 |

### CI/CD 파이프라인 관점

```
Dockerfile 방식:
  코드 변경 → bootJar → docker build → docker push → 배포

Buildpacks 방식:
  코드 변경 → bootBuildImage (내부에서 빌드+이미지 생성) → docker push → 배포
```

Buildpacks는 단계가 하나 줄고, `Dockerfile` 관련 파이프라인 스크립트를 관리할 필요가 없습니다.

---

## 4. 언제 무엇을 선택할까?

### Dockerfile을 선택하는 경우

- 베이스 이미지를 세밀하게 제어해야 할 때 (특수 OS 패키지 설치 등)
- 빌드 과정에 커스텀 스크립트 실행이 필요할 때
- 빌드팩이 지원하지 않는 런타임 환경을 사용할 때
- 컨테이너 이미지 빌드 원리를 직접 학습하고 싶을 때

### Buildpacks를 선택하는 경우

- 보안 패치를 자동화하고 싶을 때 (운영 부담 감소)
- Dockerfile 관리를 최소화하고 싶을 때
- 표준적인 Spring Boot 애플리케이션을 빠르게 컨테이너화할 때
- OCI 표준을 따르는 이미지가 필요할 때 (Kubernetes, Knative 등)

> **실무 팁**: 팀 내에 컨테이너 전문가가 없다면 Buildpacks가 유리합니다. 보안 패치와 이미지 최적화를 자동으로 처리해주어 운영 부담이 줄어듭니다. 반면 특수한 요구사항이 있거나 이미지를 완전히 제어해야 하는 경우 Dockerfile이 더 유연합니다.

---

## 5. 15-Factor 관점

### [15-Factor #5 Build, Release, Run]

빌드 단계는 재현 가능하고 명확해야 합니다.

- **Dockerfile**: 빌드 단계를 코드로 명시 → 재현 가능하지만 직접 관리 필요
- **Buildpacks**: 빌드 로직을 빌드팩에 위임 → 재현 가능하며 자동 최신화

### [15-Factor #6 Processes]

애플리케이션은 무상태(stateless) 프로세스로 실행되어야 합니다.

두 방식 모두 컨테이너 내에서 단일 프로세스로 실행됩니다. Buildpacks는 비루트 사용자, 읽기 전용 파일시스템 등의 모범 사례를 기본 적용하여 이 원칙을 더 잘 지원합니다.

---

## 참고 자료

- [Paketo Buildpacks 공식 문서](https://paketo.io/docs/)
- [Cloud Native Buildpacks 사양](https://buildpacks.io/docs/reference/spec/platform-api/)
- [Spring Boot - Container Images 공식 문서](https://docs.spring.io/spring-boot/reference/packaging/container-images/index.html)
- [Spring Boot Gradle Plugin - bootBuildImage](https://docs.spring.io/spring-boot/gradle-plugin/packaging-oci-image.html)
