plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.11"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "org.polarbookshop"
version = "0.0.1-SNAPSHOT"
description = "catalog-service"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories { // 의존 라이브러리를 검색할 아티팩트 저장소
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    /**
     * [클라우드 네이티브 스프링 - Spring Data JDBC]
     * JPA보다 가벼운 데이터 접근 기술
     * - 도메인 주도 설계(DDD)에 적합한 단순한 매핑
     * - 지연 로딩, 캐시, 더티 체킹 없음 (명시적 동작)
     * - Aggregate Root 중심의 설계 권장
     *
     * [15-Factor #4 Backing Services]
     * 데이터베이스를 연결된 리소스로 취급
     * - 설정 변경만으로 로컬 DB에서 클라우드 DB로 전환 가능
     */
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")

    /**
     * [클라우드 네이티브 - PostgreSQL]
     * 클라우드 환경에서 널리 사용되는 오픈소스 RDBMS
     * - AWS RDS, GCP Cloud SQL, Azure Database 등에서 지원
     * - JSON, 전문 검색 등 풍부한 기능 제공
     *
     * runtimeOnly: 컴파일에는 불필요, 런타임에만 필요한 드라이버
     */
    runtimeOnly("org.postgresql:postgresql")

    /**
     * [클라우드 네이티브 스프링 - Flyway]
     * 데이터베이스 마이그레이션 도구
     * - 버전 관리된 SQL 스크립트로 스키마 변경 관리
     * - 자동으로 마이그레이션 이력 추적 (flyway_schema_history 테이블)
     * - 롤백, 검증, 베이스라인 등 다양한 기능 제공
     *
     * [15-Factor #5 Build, Release, Run]
     * 스키마 변경을 코드와 함께 버전 관리
     * - 릴리스마다 일관된 스키마 보장
     * - 여러 환경(dev, staging, prod)에서 동일한 마이그레이션 적용
     */
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")

    /**
     * [클라우드 네이티브 스프링 - Testcontainers]
     * 테스트용 Docker 컨테이너를 자동으로 관리
     * - 실제 PostgreSQL과 동일한 환경에서 테스트
     * - H2 같은 인메모리 DB와 달리 프로덕션 환경과 일치
     *
     * [15-Factor #10 Dev/Prod Parity]
     * 개발/테스트 환경을 프로덕션과 최대한 유사하게 유지
     */
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

/**
 * [클라우드 네이티브 스프링 - Cloud Native Buildpacks]
 * Dockerfile 없이 컨테이너 이미지를 빌드하는 방법
 *
 * Java의 Maven/Gradle 플러그인 방식과 비교:
 * - Java(Maven): spring-boot:build-image
 * - Kotlin/Gradle: tasks.bootBuildImage { ... }
 *
 * 빌드팩 방식의 장점:
 * - Dockerfile을 직접 관리할 필요 없음 (보안 패치 자동 적용)
 * - OCI 표준 이미지 생성 (Docker 외 런타임도 지원)
 * - 레이어 최적화, 비루트 사용자 실행 등 모범 사례가 기본 적용
 * - Paketo Buildpacks 사용: https://paketo.io
 *
 * [15-Factor #5 Build, Release, Run]
 * 빌드 단계를 표준화하여 재현 가능한 이미지 생성
 *
 * 실행 방법: ./gradlew bootBuildImage
 * 생성 이미지: catalog-service:0.0.1-SNAPSHOT
 */
tasks.bootBuildImage {
    // 생성할 이미지 이름 지정 (기본값: project.name:project.version)
    imageName.set("${project.name}:${project.version}")

    /**
     * [빌더 이미지 명시 지정]
     * Spring Boot 3.x의 기본 빌더: paketobuildpacks/builder-noble-java-tiny
     * macOS + Docker Desktop 환경에서 이미지 캐시 문제로 아래 에러가 발생할 수 있음:
     *   "No 'io.buildpacks.builder.metadata' label found in image config labels"
     *
     * 원인: Docker가 이미지를 pull했지만 Spring Boot가 CNB 메타데이터 라벨을 읽지 못함
     * 해결: 빌더를 명시적으로 지정하면 Spring Boot가 이미지를 직접 참조하여 안정적으로 동작
     *
     * [실무 팁]
     * CI/CD 환경에서는 항상 빌더 이미지를 명시하는 것이 권장됨
     * → 빌더 버전이 묵시적으로 업그레이드되어 빌드가 깨지는 것을 방지
     */
    builder.set("paketobuildpacks/builder-noble-java-tiny:latest")

    /**
     * [Apple Silicon(ARM64) macOS 환경 대응]
     * Paketo 빌더는 linux/amd64 기준으로 빌드됨
     * ARM64 Mac에서 pull 시 아키텍처 불일치로 CNB 메타데이터 라벨을 못 읽는 문제 발생:
     *   "No 'io.buildpacks.builder.metadata' label found in image config labels ''"
     *
     * 해결: 빌더와 실행 이미지를 linux/amd64로 명시
     * - 컨테이너는 어차피 linux/amd64 기반 서버(EC2, GKE 등)에 배포되므로 실용적인 선택
     */
    imagePlatform.set("linux/amd64")

    /**
     * [실무 팁 - 비루트 사용자]
     * 빌드팩은 기본적으로 비루트 사용자(CNB)로 실행됨
     * Dockerfile의 `RUN useradd spring; USER spring`과 동일한 효과
     * 컨테이너 보안 모범 사례 자동 적용
     */
    environment.set(
        mapOf(
            // JVM 메모리 설정을 환경 변수로 주입 (빌드팩이 자동 계산하도록 비워둘 수도 있음)
            "BP_JVM_VERSION" to "17"
        )
    )

    docker {
        publishRegistry {
            username.set(project.findProperty("registryUsername") as String)
            password.set(project.findProperty("registryToken") as String)
            url.set(project.findProperty("registryUrl") as String)
        }
    }
}
