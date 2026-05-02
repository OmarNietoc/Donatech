package com.donatech.catalog.config;

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
                        .title("Catalog Service — Donatech")
                        .version("1.0.0")
                        .description("Gestión de kits de donación, productos, unidades y campañas de donación.")
                        .contact(new Contact()
                                .name("Donatech")
                                .email("contacto@donatech.cl")));
    }
}
