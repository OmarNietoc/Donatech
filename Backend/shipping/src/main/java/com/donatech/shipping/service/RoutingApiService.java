package com.donatech.shipping.service;

import com.donatech.shipping.exception.ExternalApiException;
import com.donatech.shipping.model.Route;
import com.donatech.shipping.model.Shipment;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

@Slf4j
@Service
public class RoutingApiService {

    private static final String ROUTING_SERVICE = "routingService";

    @Value("${routing.osrm-base-url}")
    private String osrmBaseUrl;

    @Value("${routing.nominatim-base-url}")
    private String nominatimBaseUrl;

    private final ObjectMapper objectMapper;
    private RestClient restClient;

    public RoutingApiService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void init() {
        this.restClient = RestClient.builder()
                .defaultHeader("User-Agent", "Donatech/1.0 contacto@donatech.cl")
                .build();
    }

    @CircuitBreaker(name = ROUTING_SERVICE, fallbackMethod = "fallbackCalculateRoute")
    public String fetchOptimizedPath(Route route, List<Shipment> shipments) {
        List<Shipment> withCoords = shipments.stream()
                .filter(s -> s.getLatitude() != null && s.getLongitude() != null)
                .toList();

        if (withCoords.isEmpty()) {
            log.warn("Ningún shipment tiene coordenadas — usando fallback local");
            return buildFallbackJson(shipments.size());
        }

        double[] origin = geocodeAddress(route.getOriginAddress());
        String coordString = buildCoordinateString(origin, withCoords);

        URI osrmUri = UriComponentsBuilder.fromUriString(osrmBaseUrl + "/trip/v1/driving/" + coordString)
                .queryParam("roundtrip", "false")
                .queryParam("source", "first")
                .queryParam("destination", "last")
                .queryParam("steps", "false")
                .queryParam("annotations", "false")
                .build(true)
                .toUri();

        log.info("Llamando OSRM trip: {}", osrmUri);

        @SuppressWarnings("unchecked")
        Map<String, Object> osrmResponse = restClient.get()
                .uri(osrmUri)
                .retrieve()
                .body(Map.class);

        return buildResultJson(osrmResponse, withCoords, shipments.size());
    }

    @SuppressWarnings("unchecked")
    private double[] geocodeAddress(String address) {
        URI uri = UriComponentsBuilder.fromUriString(nominatimBaseUrl + "/search")
                .queryParam("q", address)
                .queryParam("format", "json")
                .queryParam("limit", "1")
                .build()
                .toUri();

        log.info("Geocodificando con Nominatim: {}", address);

        List<Map<String, Object>> results = restClient.get()
                .uri(uri)
                .retrieve()
                .body(List.class);

        if (results == null || results.isEmpty()) {
            throw new ExternalApiException("Nominatim no encontró resultados para: " + address);
        }

        Map<String, Object> first = results.get(0);
        double lat = Double.parseDouble((String) first.get("lat"));
        double lon = Double.parseDouble((String) first.get("lon"));
        log.info("Origen geocodificado: lat={}, lon={}", lat, lon);
        return new double[]{lat, lon};
    }

    // OSRM espera formato "lon,lat;lon,lat;..."
    private String buildCoordinateString(double[] origin, List<Shipment> shipments) {
        StringBuilder sb = new StringBuilder();
        sb.append(origin[1]).append(",").append(origin[0]);
        for (Shipment s : shipments) {
            sb.append(";").append(s.getLongitude()).append(",").append(s.getLatitude());
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String buildResultJson(Map<String, Object> osrmResponse, List<Shipment> withCoords, int totalShipments) {
        try {
            List<Map<String, Object>> trips = (List<Map<String, Object>>) osrmResponse.get("trips");
            List<Map<String, Object>> waypoints = (List<Map<String, Object>>) osrmResponse.get("waypoints");

            Map<String, Object> trip = trips.get(0);
            double distanceMeters = ((Number) trip.get("distance")).doubleValue();
            double durationSeconds = ((Number) trip.get("duration")).doubleValue();

            // waypoints[0] = origen (skip); waypoints[1..n] = shipments en orden de input
            // waypoint_index = posición en el trip optimizado (0=origen, 1..n=paradas)
            String[] optimizedOrder = new String[withCoords.size()];
            for (int i = 0; i < withCoords.size(); i++) {
                Map<String, Object> wp = waypoints.get(i + 1);
                int tripPos = ((Number) wp.get("waypoint_index")).intValue();
                if (tripPos > 0 && tripPos <= withCoords.size()) {
                    optimizedOrder[tripPos - 1] = withCoords.get(i).getId();
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("source", "OSRM");
            result.put("distance_km", Math.round(distanceMeters / 100.0) / 10.0);
            result.put("duration_minutes", (int) Math.round(durationSeconds / 60.0));
            result.put("total_stops", totalShipments);
            result.put("routed_stops", withCoords.size());
            result.put("optimized_order", Arrays.asList(optimizedOrder));

            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            log.error("Error parseando respuesta OSRM: {}", e.getMessage());
            return buildFallbackJson(totalShipments);
        }
    }

    private String buildFallbackJson(int totalShipments) {
        try {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("source", "fallback");
            result.put("total_stops", totalShipments);
            result.put("routed_stops", 0);
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return "{\"source\":\"fallback\"}";
        }
    }

    public String fallbackCalculateRoute(Route route, List<Shipment> shipments, Throwable t) {
        log.warn("CircuitBreaker activo para routingService: {}", t.getMessage());
        return buildFallbackJson(shipments.size());
    }
}
