package org.polarbookshop.catalogservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * [클라우드 네이티브 스프링 - 애플리케이션 진입점]
 * @SpringBootApplication: 세 가지 어노테이션의 조합
 * - @Configuration: 빈 정의를 위한 설정 클래스
 * - @EnableAutoConfiguration: 클래스패스 기반 자동 설정 활성화
 * - @ComponentScan: 현재 패키지 하위의 컴포넌트 자동 스캔
 *
 * [실무 조언]
 * - 메인 클래스는 루트 패키지에 위치시켜 하위 패키지 전체 스캔
 * - 클라우드 환경에서는 12-Factor App 원칙 준수 권장
 *
 * [Kotlin - 클래스 본문 생략]
 * 본문이 없는 클래스는 중괄호 생략 가능
 * Java: public class CatalogServiceApplication { }
 */
@SpringBootApplication
class CatalogServiceApplication

/**
 * [Kotlin - 최상위 함수 (Top-level Function)]
 * 클래스 외부에 함수 정의 가능 - Java의 static 메서드와 유사하게 동작
 * 컴파일 시 CatalogServiceApplicationKt 클래스의 static 메서드로 변환됨
 *
 * [Kotlin - 스프레드 연산자 (*)]
 * *args: 배열을 가변인자(vararg)로 전달
 * Java에서는 배열을 그대로 전달 가능하지만, Kotlin은 명시적 변환 필요
 *
 * [클라우드 네이티브 스프링 - runApplication]
 * Spring Boot의 Kotlin 확장 함수
 * - 내장 톰캣 서버 시작
 * - ApplicationContext 초기화
 * - 자동 설정 적용
 */
fun main(args: Array<String>) {
    runApplication<CatalogServiceApplication>(*args)
}
