package org.polarbookshop.catalogservice.domain

/**
 * [클라우드 네이티브 스프링 - 비즈니스 규칙 예외]
 * 중복 ISBN 등록 시도 시 발생하는 예외
 * - HTTP 422 Unprocessable Entity로 매핑됨
 * - 요청 형식은 올바르지만 비즈니스 규칙 위반
 *
 * [실무 조언]
 * - 400 Bad Request: 요청 형식/문법 오류
 * - 422 Unprocessable Entity: 형식은 맞지만 처리 불가 (비즈니스 규칙 위반)
 * - 409 Conflict: 리소스 상태 충돌 (동시 수정 등)
 */
class BookAlreadyExistsException(isbn: String) : RuntimeException("The book with ISBN $isbn already exists in the catalog")