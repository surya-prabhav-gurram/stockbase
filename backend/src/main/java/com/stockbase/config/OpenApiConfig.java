package com.stockbase.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI metadata for the interactive API docs served at /swagger-ui.html.
 * Declares the Bearer-JWT scheme so the "Authorize" button in Swagger UI lets a
 * reviewer paste a token and exercise the protected endpoints directly.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearer-jwt";

    @Bean
    public OpenAPI stockbaseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("StockBase Inventory API")
                        .description("REST API for inventory, stock transactions, and reporting.")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER))
                .components(new Components().addSecuritySchemes(BEARER,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
