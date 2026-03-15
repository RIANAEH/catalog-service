package org.polarbookshop.catalogservice.web

import org.polarbookshop.catalogservice.config.PolarProperties
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
 *
 * [Kotlin - 주 생성자 의존성 주입]
 * 클래스 선언부에서 바로 생성자 파라미터로 의존성 주입
 * - val로 선언하면 자동으로 프로퍼티가 됨
 * - @Autowired 없이도 Spring이 자동 주입 (생성자가 하나일 때)
 *
 * Java로 작성했다면:
 * ```java
 * @RestController
 * public class HomeController {
 *     private final PolarProperties polarProperties;
 *
 *     @Autowired // Spring 4.3+에서는 생략 가능
 *     public HomeController(PolarProperties polarProperties) {
 *         this.polarProperties = polarProperties;
 *     }
 * }
 * ```
 */
@RestController
class HomeController(
    private val polarProperties: PolarProperties
) {

    /**
     * [클라우드 네이티브 - 외부 설정 활용]
     * 하드코딩된 값 대신 외부 설정에서 읽어온 값 사용
     * - 환경별로 다른 메시지 표시 가능 (dev, staging, prod)
     * - 재배포 없이 ConfigMap 변경으로 메시지 수정 가능
     */
    @GetMapping("/")
    fun greet(): String = polarProperties.greeting
}
