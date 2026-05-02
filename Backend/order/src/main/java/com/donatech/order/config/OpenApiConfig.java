package com.donatech.order.config;

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
                        .title("Order Service — Donatech")
                        .version("1.0.0")
                        .description("Gestión de órdenes de donación, historial de estados, comprobantes de transferencia y confirmación de entrega.")
                        .contact(new Contact()
                                .name("Donatech")
                                .email("contacto@donatech.cl")));
    }
}
