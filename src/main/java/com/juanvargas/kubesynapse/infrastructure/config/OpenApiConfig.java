package com.juanvargas.kubesynapse.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Springdoc OpenAPI configuration for Swagger UI (/swagger-ui.html).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("KubeSynapse AI — Kubernetes Autonomous Engine")
                        .version("1.0.0")
                        .description("Motor de Diagnóstico y Remediación Autónoma para clusters Kubernetes desarrollado en Java 21, Spring Boot y Arquitectura Hexagonal.")
                        .contact(new Contact()
                                .name("Juan Vargas")
                                .url("https://vargasjuanj.github.io"))
                        .license(new License().name("Apache 2.0")));
    }
}
