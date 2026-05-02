package com.donatech.shipping.service;

import com.donatech.shipping.model.Route;
import com.donatech.shipping.exception.ExternalApiException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RoutingApiService {

    private static final String ROUTING_SERVICE = "routingService";

    @CircuitBreaker(name = ROUTING_SERVICE, fallbackMethod = "fallbackCalculateRoute")
    public String fetchOptimizedPath(Route route) {
        log.info("Llamando a la API de enrutamiento externa (OSRM/Nominatim) para la ruta: {}", route.getId());
        
        // Simulación: En producción esto llamaría a OSRM o Nominatim
        return "{\"path_points\": [{\"lat\": 10.0, \"lng\": 20.0}, {\"lat\": 10.5, \"lng\": 20.5}], \"distance_km\": 15.4}";
    }

    public String fallbackCalculateRoute(Route route, Throwable t) {
        log.warn("Ejecutando método Fallback. Razón: {}", t.getMessage());
        // Respuesta por defecto con un path directo (línea recta) u otra heurística simple
        return "{\"path_points\": [], \"distance_km\": 0, \"info\": \"Calculated via fallback (Direct Line)\"}";
    }
}

