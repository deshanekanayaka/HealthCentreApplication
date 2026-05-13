package com.westminster.healthcentre;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Westminster Health Centre — Spring Boot entry point.
 *
 * <p>{@code @SpringBootApplication} is shorthand for three annotations:
 * <ul>
 *   <li>{@code @Configuration}  — this class can define Spring beans
 *   <li>{@code @EnableAutoConfiguration} — Spring Boot auto-configures JPA, web, etc.
 *   <li>{@code @ComponentScan} — scans this package and sub-packages for components
 * </ul>
 *
 * <p>Run with: {@code mvn spring-boot:run}
 * <br>Or after building: {@code java -jar target/health-centre-backend-1.0.0.jar}
 * <br>Swagger UI: {@code http://localhost:8080/swagger-ui/index.html}
 */
@SpringBootApplication
public class HealthCentreApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthCentreApplication.class, args);
    }
}
