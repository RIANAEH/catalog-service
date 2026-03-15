package org.polarbookshop.catalogservice.web

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * [클라우드 네이티브 스프링 - 헬스 체크 엔드포인트]
 * 루트 경로에 간단한 응답을 제공하는 컨트롤러
 * - 서비스 가용성 확인에 활용
 * - 로드밸런서, 쿠버네티스 등에서 헬스 체크 용도
 *
 * [실무 조언]
 * - 프로덕션에서는 Spring Boot Actuator의 /actuator/health 사용 권장
 * - Actuator는 상세한 헬스 정보 제공 (DB 연결, 디스크 공간 등)
 * - 쿠버네티스 liveness/readiness probe와 연동
 */
@RestController
class HomeController {

    /**
     * [Kotlin - 단일 표현식 함수]
     * 함수 본문이 단일 표현식이면 = 로 간결하게 작성 가능
     * 반환 타입도 추론 가능하면 생략 가능
     *
     * 아래와 동일:
     * fun greet(): String {
     *     return "Welcome to the Polar Bookshop Catalog Service!"
     * }
     */
    @GetMapping("/")
    fun greet(): String {
        return "Welcome to the Polar Bookshop Catalog Service!"
    }
}
