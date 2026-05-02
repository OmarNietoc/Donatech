package com.donatech.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Authentication Service — Donatech")
                        .version("1.0.0")
                        .description("Servicio de autenticación y registro de usuarios. Gestiona JWT, roles y acceso a la plataforma.")
                        .contact(new Contact()
                                .name("Donatech")
                                .email("contacto@donatech.cl")));
    }
}
