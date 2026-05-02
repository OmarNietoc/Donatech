package com.donatech.shipping.config;

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
                        .title("Shipping Service — Donatech")
                        .version("1.0.0")
                        .description("Gestión de envíos y rutas de entrega. Optimización de rutas con OSRM y geocodificación con Nominatim.")
                        .contact(new Contact()
                                .name("Donatech")
                                .email("contacto@donatech.cl")));
    }
}
