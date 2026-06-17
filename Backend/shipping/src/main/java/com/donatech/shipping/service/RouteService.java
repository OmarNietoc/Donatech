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
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final ShipmentRepository shipmentRepository;
    private final Map<String, ShippingCalculationStrategy> calculationStrategies;
    private final ObjectMapper objectMapper;
    private final com.donatech.shipping.event.ShippingEventPublisher eventPublisher;

    @Transactional
    public Route createRoute(String companyId, String carrierId, Long collaboratorId,
                             String collaboratorNombre, String collaboratorEmail,
                             String originAddress, List<String> shipmentIds, boolean optimizeRoute) {
        log.info("Creando ruta para colaborador: {}", collaboratorId);

        Route route = Route.builder()
                .companyId(companyId)
                .carrierId(carrierId)
                .collaboratorId(collaboratorId)
                .collaboratorNombre(collaboratorNombre)
                .collaboratorEmail(collaboratorEmail)
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

        // IDs de órdenes asociadas (reutilizados para el nombre legible y el evento route.assigned).
        List<Long> orderIds = shipments.stream()
                .map(s -> {
                    try { return Long.parseLong(s.getOrderId()); } catch (NumberFormatException e) { return null; }
                })
                .filter(java.util.Objects::nonNull)
                .toList();
        route.setName(buildRouteName(orderIds));

        String strategyKey = determineStrategyKey(carrierId);
        ShippingCalculationStrategy strategy = calculationStrategies.get(strategyKey);

        String pathJson = strategy != null
                ? strategy.calculateRoute(route, shipments, optimizeRoute)
                : buildFallbackJson(shipments.size());
        route.setOptimizedPathJson(pathJson);

        Route saved = routeRepository.save(route);

        // Notificar a order (cambiar estado de las órdenes a ASIGNADA_ENVIO) y al colaborador (email).
        eventPublisher.publishRouteAssigned(new com.donatech.shipping.event.RouteAssignedEvent(
                saved.getId(), saved.getName(), orderIds, collaboratorId, collaboratorNombre, collaboratorEmail));

        return saved;
    }

    /** Nombre legible "Route {fecha}_{orderIds unidos por '-'}", truncado a 33 caracteres. */
    private String buildRouteName(List<Long> orderIds) {
        String ids = orderIds.stream().map(String::valueOf).collect(Collectors.joining("-"));
        String name = "Route " + LocalDate.now() + "_" + ids;
        return name.length() > 33 ? name.substring(0, 33) : name;
    }

    public List<Route> getAllRoutes(String companyId, RouteStatus status) {
        List<Route> routes;
        if (companyId != null && status != null) {
            routes = routeRepository.findByCompanyIdAndStatus(companyId, status);
        } else if (companyId != null) {
            routes = routeRepository.findByCompanyId(companyId);
        } else if (status != null) {
            routes = routeRepository.findByStatus(status);
        } else {
            routes = routeRepository.findAll();
        }
        // Orden cronológico inverso (último creado primero).
        routes.sort(Comparator.comparing(Route::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return routes;
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

    // ───────────────── Ciclo de vida event-driven (consumido desde order ms) ─────────────────

    private static final Set<DeliveryStatus> TERMINAL_STATES =
            EnumSet.of(DeliveryStatus.DELIVERED, DeliveryStatus.FAILED, DeliveryStatus.CANCELLED);

    /** order.shipped: el envío sale a reparto. Mueve el shipment a DISPATCHED y arranca la ruta. */
    @Transactional
    public void onOrderShipped(Long orderId) {
        Shipment shipment = findShipmentByOrder(orderId);
        if (shipment == null) return;
        if (shipment.getDeliveryStatus().canTransitionTo(DeliveryStatus.DISPATCHED)) {
            shipment.setDeliveryStatus(DeliveryStatus.DISPATCHED);
            shipmentRepository.save(shipment);
        }
        Route route = shipment.getRoute();
        if (route != null && route.getStatus() == RouteStatus.PLANNED) {
            route.setStatus(RouteStatus.IN_PROGRESS);
            routeRepository.save(route);
            log.info("Ruta {} pasó a IN_PROGRESS por order.shipped (orden {})", route.getId(), orderId);
        }
    }

    /** order.delivered: el envío se entregó. Marca DELIVERED y reevalúa el cierre de la ruta. */
    @Transactional
    public void onOrderDelivered(Long orderId) {
        Shipment shipment = findShipmentByOrder(orderId);
        if (shipment == null) return;
        if (shipment.getDeliveryStatus().canTransitionTo(DeliveryStatus.DELIVERED)) {
            shipment.setDeliveryStatus(DeliveryStatus.DELIVERED);
            shipment.setActualDelivery(LocalDateTime.now());
            shipmentRepository.save(shipment);
        }
        checkRouteCompletion(shipment.getRoute());
    }

    /** donation.cancelled: la donación se canceló. Marca el envío CANCELLED y reevalúa la ruta. */
    @Transactional
    public void onDonationCancelled(Long orderId) {
        Shipment shipment = findShipmentByOrder(orderId);
        if (shipment == null) return;
        shipment.setDeliveryStatus(DeliveryStatus.CANCELLED);
        shipmentRepository.save(shipment);
        checkRouteCompletion(shipment.getRoute());
    }

    /** Cierra la ruta cuando todos sus envíos están en estado terminal:
     *  COMPLETED si al menos uno fue entregado, CANCELLED si ninguno. */
    private void checkRouteCompletion(Route route) {
        if (route == null) return;
        List<Shipment> shipments = shipmentRepository.findByRouteId(route.getId());
        if (shipments.isEmpty()) return;
        boolean allTerminal = shipments.stream()
                .allMatch(s -> TERMINAL_STATES.contains(s.getDeliveryStatus()));
        if (!allTerminal) return;
        boolean anyDelivered = shipments.stream()
                .anyMatch(s -> s.getDeliveryStatus() == DeliveryStatus.DELIVERED);
        route.setStatus(anyDelivered ? RouteStatus.COMPLETED : RouteStatus.CANCELLED);
        routeRepository.save(route);
        log.info("Ruta {} cerrada como {}", route.getId(), route.getStatus());
    }

    private Shipment findShipmentByOrder(Long orderId) {
        Shipment shipment = shipmentRepository.findByOrderId(String.valueOf(orderId)).orElse(null);
        if (shipment == null) {
            log.warn("No existe shipment para la orden {} (evento de ciclo de ruta ignorado)", orderId);
        }
        return shipment;
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
