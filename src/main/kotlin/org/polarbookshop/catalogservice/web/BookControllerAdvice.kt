package org.polarbookshop.catalogservice.web

import org.polarbookshop.catalogservice.domain.BookAlreadyExistsException
import org.polarbookshop.catalogservice.domain.BookNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * [클라우드 네이티브 스프링 - 전역 예외 처리]
 * @RestControllerAdvice: 모든 컨트롤러에 적용되는 전역 예외 핸들러
 * - @ControllerAdvice + @ResponseBody
 * - 예외를 일관된 형식의 응답으로 변환
 * - 중앙 집중식 에러 처리로 코드 중복 제거
 *
 * [실무 조언]
 * - 클라이언트에게 일관된 에러 응답 형식 제공
 * - 내부 예외 정보 노출 주의 (보안)
 * - 마이크로서비스 환경에서는 에러 응답 표준화 중요
 * - RFC 7807 Problem Details 형식 고려
 */
@RestControllerAdvice
class BookControllerAdvice {

    /**
     * [클라우드 네이티브 스프링 - 예외 핸들러]
     * @ExceptionHandler: 특정 예외 타입 처리
     * - 해당 예외 발생 시 이 메서드가 호출됨
     * - 반환값이 HTTP 응답 본문이 됨
     *
     * @ResponseStatus: HTTP 상태 코드 지정
     * - NOT_FOUND (404): 리소스를 찾을 수 없음
     *
     * [Kotlin - 엘비스 연산자 활용]
     * ex.message ?: "기본값": message가 null이면 기본값 사용
     */
    @ExceptionHandler(BookNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleBookNotFoundException(ex: BookNotFoundException): String {
        return ex.message ?: "Book not found"
    }

    /**
     * [클라우드 네이티브 스프링 - HTTP 상태 코드]
     * UNPROCESSABLE_ENTITY (422): 요청은 이해했으나 처리 불가
     * - 비즈니스 규칙 위반 (중복 ISBN)
     * - 400 Bad Request와 구분: 문법은 맞지만 의미상 처리 불가
     */
    @ExceptionHandler(BookAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    fun handleBookAlreadyExistsException(ex: BookAlreadyExistsException): String {
        return ex.message ?: "Book already exists"
    }

    /**
     * [클라우드 네이티브 스프링 - Validation 예외 처리]
     * MethodArgumentNotValidException: @Valid 검증 실패 시 발생
     * - bindingResult: 검증 실패 정보 포함
     * - fieldErrors: 필드별 에러 목록
     *
     * [Kotlin - joinToString]
     * 컬렉션 요소를 문자열로 연결
     * - separator: 구분자 (기본값 ", ")
     * - 람다로 각 요소 변환 방식 지정
     *
     * Java 방식:
     * ```java
     * return ex.getBindingResult().getFieldErrors().stream()
     *     .map(e -> e.getField() + ": " + e.getDefaultMessage())
     *     .collect(Collectors.joining(", "));
     * ```
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): String {
        return ex.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
    }
}