package org.polarbookshop.catalogservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CatalogServiceApplication

/**
 * 애플리케이션 시작
 * - 내장된 톰캣 서버가 실행되고, 애플리케이션 컨텍스트가 초기화됨
 *   - Tomcat initialized with port 8080 (http)
 *   - Initializing Spring embedded WebApplicationContext
 */
fun main(args: Array<String>) {
    runApplication<CatalogServiceApplication>(*args)
}
