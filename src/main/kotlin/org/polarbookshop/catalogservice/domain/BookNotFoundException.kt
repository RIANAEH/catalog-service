package org.polarbookshop.catalogservice.domain

/**
 * [클라우드 네이티브 스프링 - 도메인 예외]
 * 비즈니스 로직에서 발생하는 예외를 도메인 계층에 정의
 * - 기술적 예외와 비즈니스 예외 분리
 * - @ControllerAdvice에서 HTTP 상태 코드로 매핑
 *
 * [실무 조언]
 * - 도메인 예외는 비즈니스 의미를 담아야 함
 * - 예외 메시지는 사용자/개발자 모두 이해할 수 있게 작성
 * - 마이크로서비스 간 통신 시 에러 코드 표준화 고려
 *
 * [Kotlin - 단일 표현식 클래스]
 * 클래스 본문 없이 주 생성자만으로 정의 가능
 * - 간결한 예외 클래스 정의에 유용
 *
 * [Kotlin - 문자열 템플릿]
 * "$isbn": 변수 값을 문자열에 삽입
 * Java: "The book with ISBN " + isbn + " was not found..."
 *
 * [Kotlin - 상속]
 * 콜론(:)으로 부모 클래스 상속
 * RuntimeException(message): 부모 생성자 호출
 * Java: extends RuntimeException
 */
class BookNotFoundException(isbn: String) : RuntimeException("The book with ISBN $isbn was not found in the catalog")
