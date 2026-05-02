package com.donatech.shipping.service;

import com.donatech.shipping.enums.DeliveryStatus;
import com.donatech.shipping.enums.RouteStatus;
import com.donatech.shipping.model.Route;
import com.donatech.shipping.model.Shipment;
import com.donatech.shipping.repository.RouteRepository;
import com.donatech.shipping.repository.ShipmentRepository;
import com.donatech.shipping.strategy.ShippingCalculationStrategy;
import com.donatech.shipping.exception.RouteNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final ShipmentRepository shipmentRepository;
    private final RoutingApiService routingApiService;
    private final Map<String, ShippingCalculationStrategy> calculationStrategies;

    @Transactional
    public Route createRoute(String companyId, String carrierId, String originAddress, List<String> shipmentIds) {
        log.info("Creando ruta para la compañía: {}", companyId);

        Route route = Route.builder()
                .companyId(companyId)
                .carrierId(carrierId)
                .routeDate(LocalDate.now())
                .originAddress(originAddress)
                .status(RouteStatus.PLANNED)
                .build();

        // Buscar pedidos por ID y asociarlos
        List<Shipment> shipments = shipmentRepository.findAllById(shipmentIds);
        if (shipments.size() != shipmentIds.size()) {
            throw new com.donatech.shipping.exception.ShipmentNotFoundException(
                    "Algunos de los IDs de envíos proporcionados no existen o son inválidos.");
        }

        for (Shipment shipment : shipments) {
            // Regla de negocio: Solo admitir paquetes PENDING y sin ruta asignada
            if (shipment.getDeliveryStatus() != DeliveryStatus.PENDING || shipment.getRoute() != null) {
                throw new IllegalStateException(
                        "El envío con ID " + shipment.getId() + " ya está asignado a otra ruta o no está disponible.");
            }
            shipment.setRoute(route);
            shipment.setDeliveryStatus(DeliveryStatus.ASSIGNED);
            route.getShipments().add(shipment);
        }

        // Determinar qué estrategia usar (DhlStrategy, LocalCarrierStrategy, etc.
        // basado en carrierId)
        String strategyKey = determineStrategyKey(carrierId);
        ShippingCalculationStrategy strategy = calculationStrategies.get(strategyKey);

        if (strategy != null) {
            String routePlan = strategy.calculateRoute(route);
            log.info("Plan de ruta comercial generado: {}", routePlan);
        }

        // Llamar a la API externa para el path geográfico, protegido con CircuitBreaker
        String pathJson = routingApiService.fetchOptimizedPath(route);
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
    public void deleteRoute(String id) {
        Route route = getRouteById(id);

        // Orphan Management (Desasignación)
        List<Shipment> shipments = route.getShipments();
        if (shipments != null) {
            for (Shipment shipment : shipments) {
                shipment.setRoute(null);
                shipment.setDeliveryStatus(DeliveryStatus.PENDING);
                shipmentRepository.save(shipment);
            }
            route.getShipments().clear();
        }

        // Soft Delete de la ruta
        route.setStatus(RouteStatus.CANCELLED);
        routeRepository.save(route);
    }

    private String determineStrategyKey(String carrierId) {
        // En una aplicación real esto podría basarse en configuraciones de DB.
        if ("DHL".equalsIgnoreCase(carrierId)) {
            return "dhlStrategy";
        }
        return "localCarrierStrategy"; // Default
    }
}

