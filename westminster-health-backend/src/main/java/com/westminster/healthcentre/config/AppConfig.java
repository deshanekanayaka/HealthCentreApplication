package com.westminster.healthcentre.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Application configuration beans.
 *
 * <h2>CORS</h2>
 * <p>Allows the React frontend (running on a different origin) to call the API.
 * Allowed origins are read from the {@code CORS_ALLOWED_ORIGINS} environment
 * variable so the same JAR works in development (localhost:5173) and in
 * production (the deployed Vercel URL) without code changes.
 *
 * <h2>OpenAPI</h2>
 * <p>SpringDoc auto-generates the spec from controller annotations. This bean
 * just enriches the metadata (title, description, contact). The Swagger UI is
 * available at {@code /swagger-ui/index.html}.
 */
@Configuration
public class AppConfig {

    // ---- CORS ---------------------------------------------------------------

    @Bean
    public WebMvcConfigurer corsConfigurer(
            @Value("${cors.allowed-origins:http://localhost:5173}") String[] allowedOrigins) {

        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(false); // no cookies — keep it stateless
            }
        };
    }

    // ---- OpenAPI ------------------------------------------------------------

    @Bean
    public OpenAPI openApiMetadata() {
        return new OpenAPI().info(
                new Info()
                        .title("Westminster Health Centre API")
                        .version("1.0.0")
                        .description("""
                                REST API for managing doctors and receptionists
                                at the Westminster Health Centre.
                                
                                Staff IDs follow the format XX0000 (two uppercase letters
                                + four digits), e.g. DR0001 for a doctor.
                                """)
                        .contact(new Contact()
                                .name("Westminster Health Centre")
                                .url("https://github.com/YOUR_USERNAME/westminster-health-centre"))
        );
    }
}
