package com.donatech.shipping.service;

import com.donatech.shipping.enums.DeliveryStatus;
import com.donatech.shipping.enums.RouteStatus;
import com.donatech.shipping.model.Route;
import com.donatech.shipping.model.Shipment;
import com.donatech.shipping.repository.RouteRepository;
import com.donatech.shipping.repository.ShipmentRepository;
import com.donatech.shipping.strategy.ShippingCalculationStrategy;
import com.donatech.shipping.exception.RouteNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final ShipmentRepository shipmentRepository;
    private final Map<String, ShippingCalculationStrategy> calculationStrategies;
    private final ObjectMapper objectMapper;

    @Transactional
    public Route createRoute(String companyId, String carrierId, String originAddress, List<String> shipmentIds, boolean optimizeRoute) {
        log.info("Creando ruta para la compañía: {}", companyId);

        Route route = Route.builder()
                .companyId(companyId)
                .carrierId(carrierId)
                .routeDate(LocalDate.now())
                .originAddress(originAddress)
                .status(RouteStatus.PLANNED)
                .build();

        List<Shipment> shipments = shipmentRepository.findAllById(shipmentIds);
        if (shipments.size() != shipmentIds.size()) {
            throw new com.donatech.shipping.exception.ShipmentNotFoundException(
                    "Algunos de los IDs de envíos proporcionados no existen o son inválidos.");
        }

        for (Shipment shipment : shipments) {
            if (shipment.getDeliveryStatus() != DeliveryStatus.PENDING || shipment.getRoute() != null) {
                throw new IllegalStateException(
                        "El envío con ID " + shipment.getId() + " ya está asignado a otra ruta o no está disponible.");
            }
            shipment.setRoute(route);
            shipment.setDeliveryStatus(DeliveryStatus.ASSIGNED);
            route.getShipments().add(shipment);
        }

        String strategyKey = determineStrategyKey(carrierId);
        ShippingCalculationStrategy strategy = calculationStrategies.get(strategyKey);

        String pathJson = strategy != null
                ? strategy.calculateRoute(route, shipments, optimizeRoute)
                : buildFallbackJson(shipments.size());
        route.setOptimizedPathJson(pathJson);

        return routeRepository.save(route);
    }

    public List<Route> getAllRoutes(String companyId, RouteStatus status) {
        if (companyId != null && status != null) {
            return routeRepository.findByCompanyIdAndStatus(companyId, status);
        } else if (companyId != null) {
            return routeRepository.findByCompanyId(companyId);
        } else if (status != null) {
            return routeRepository.findByStatus(status);
        }
        return routeRepository.findAll();
    }

    public Route getRouteById(String id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new RouteNotFoundException("La ruta con ID " + id + " no fue encontrada."));
    }

    @Transactional
    public Route updateRouteStatus(String id, RouteStatus status) {
        Route existing = getRouteById(id);
        existing.setStatus(status);

        if (status == RouteStatus.IN_PROGRESS) {
            for (Shipment shipment : existing.getShipments()) {
                shipment.setDeliveryStatus(DeliveryStatus.DISPATCHED);
                shipmentRepository.save(shipment);
            }
        }

        return routeRepository.save(existing);
    }

    @Transactional
    public Route reorderShipments(String routeId, List<String> shipmentIds) {
        Route route = getRouteById(routeId);

        if (route.getStatus() != RouteStatus.PLANNED) {
            throw new IllegalStateException(
                    "Solo se puede reordenar una ruta en estado PLANNED. Estado actual: " + route.getStatus());
        }

        Set<String> routeShipmentIds = new HashSet<>();
        for (Shipment s : route.getShipments()) {
            routeShipmentIds.add(s.getId());
        }

        Set<String> requestedIds = new HashSet<>(shipmentIds);
        if (!routeShipmentIds.equals(requestedIds)) {
            throw new IllegalArgumentException(
                    "Los IDs de envíos no coinciden exactamente con los envíos de la ruta.");
        }

        try {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("source", "reordered");
            result.put("total_stops", shipmentIds.size());
            result.put("optimized_order", shipmentIds);
            route.setOptimizedPathJson(objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            route.setOptimizedPathJson("{\"source\":\"reordered\"}");
        }

        return routeRepository.save(route);
    }

    @Transactional
    public void deleteRoute(String id) {
        Route route = getRouteById(id);

        List<Shipment> shipments = route.getShipments();
        if (shipments != null) {
            for (Shipment shipment : shipments) {
                shipment.setRoute(null);
                shipment.setDeliveryStatus(DeliveryStatus.PENDING);
                shipmentRepository.save(shipment);
            }
            route.getShipments().clear();
        }

        route.setStatus(RouteStatus.CANCELLED);
        routeRepository.save(route);
    }

    private String determineStrategyKey(String carrierId) {
        if ("DHL".equalsIgnoreCase(carrierId)) {
            return "dhlStrategy";
        }
        return "localCarrierStrategy";
    }

    private String buildFallbackJson(int totalShipments) {
        try {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("source", "fallback");
            result.put("total_stops", totalShipments);
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return "{\"source\":\"fallback\"}";
        }
    }
}
