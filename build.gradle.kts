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
