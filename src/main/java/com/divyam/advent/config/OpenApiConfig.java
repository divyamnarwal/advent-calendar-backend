package com.divyam.advent.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for API documentation.
 *
 * Once the application is running, access Swagger UI at:
 * http://localhost:8080/swagger-ui/index.html
 *
 * OpenAPI spec available at:
 * http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI adventCalendarOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Advent Calendar Backend API")
                        .description("Spring Boot REST API for managing daily challenges with mood-based recommendations")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Advent Calendar Team")
                                .email("support@adventcalendar.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
