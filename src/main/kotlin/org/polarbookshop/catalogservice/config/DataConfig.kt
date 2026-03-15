package org.polarbookshop.catalogservice.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing

/**
 * [클라우드 네이티브 스프링 - Spring Data JDBC 설정]
 * @Configuration: Spring 설정 클래스임을 명시
 * @EnableJdbcAuditing: JDBC Auditing 기능 활성화
 *
 * [클라우드 네이티브 스프링 - Auditing]
 * 엔티티의 생성/수정 시간을 자동으로 관리
 * - @CreatedDate: 엔티티 생성 시 자동으로 현재 시간 설정
 * - @LastModifiedDate: 엔티티 수정 시 자동으로 현재 시간 갱신
 * - @CreatedBy, @LastModifiedBy: 생성/수정자 추적 (AuditorAware 구현 필요)
 *
 * [실무 조언]
 * - 대부분의 엔티티에 생성/수정 시간은 필수
 * - 데이터 변경 이력 추적, 디버깅, 감사(Audit)에 활용
 * - 마이크로서비스 환경에서 데이터 동기화 시점 파악에 유용
 *
 * [Kotlin - 클래스 본문 생략]
 * 본문이 없는 클래스는 중괄호 생략 가능
 */
@Configuration
@EnableJdbcAuditing
class DataConfig
