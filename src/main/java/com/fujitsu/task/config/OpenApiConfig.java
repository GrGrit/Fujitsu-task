package com.fujitsu.task.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * General information shown in the Swagger UI at /swagger-ui.html.
 */
@Configuration
public class OpenApiConfig {

    /**
     * API title and description for the Swagger UI.
     */
    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("Delivery Fee API")
                .version("1.0")
                .description("""
                        Calculates food delivery fees from a regional base fee and weather-based extra fees.
                        Fee rules can be managed through the CRUD endpoints. Rules are versioned: updating or
                        deleting a rule ends its validity instead of removing it, so fees can be calculated
                        for past points in time with the `dateTime` parameter."""));
    }
}
