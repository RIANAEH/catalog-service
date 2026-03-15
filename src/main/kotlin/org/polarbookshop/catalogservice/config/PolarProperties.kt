package org.polarbookshop.catalogservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * [클라우드 네이티브 스프링 - 외부 설정 관리]
 * @ConfigurationProperties: application.yml의 설정을 타입 안전하게 바인딩
 * - prefix = "polar": polar.* 로 시작하는 설정을 이 클래스에 매핑
 * - @Value 어노테이션보다 구조화된 설정 관리에 유리
 * - IDE 자동완성, 유효성 검증, 메타데이터 생성 지원
 *
 * [Kotlin - data class]
 * Java의 POJO를 간결하게 표현
 * - equals(), hashCode(), toString(), copy() 자동 생성
 * - 주 생성자에서 프로퍼티 선언과 초기화를 동시에
 *
 * Java로 작성했다면:
 * ```java
 * @ConfigurationProperties(prefix = "polar")
 * public class PolarProperties {
 *     private String greeting;
 *     public String getGreeting() { return greeting; }
 *     public void setGreeting(String greeting) { this.greeting = greeting; }
 *     // equals, hashCode, toString...
 * }
 * ```
 *
 * [실무 조언]
 * - 환경별로 다른 설정이 필요한 값은 외부 설정으로 분리
 * - 12-Factor App: 설정은 환경 변수나 외부 설정 파일로 관리
 * - 쿠버네티스에서는 ConfigMap/Secret으로 주입 가능
 */
@ConfigurationProperties(prefix = "polar")
data class PolarProperties(
    /**
     * 애플리케이션 인사 메시지
     * - 환경 변수로 오버라이드: POLAR_GREETING
     */
    val greeting: String = "Welcome to the local book catalog!"
)
