package com.donatech.shipping.service;

import com.donatech.shipping.enums.DeliveryStatus;
import com.donatech.shipping.enums.RouteStatus;
import com.donatech.shipping.exception.RouteNotFoundException;
import com.donatech.shipping.model.Route;
import com.donatech.shipping.model.Shipment;
import com.donatech.shipping.repository.RouteRepository;
import com.donatech.shipping.repository.ShipmentRepository;
import com.donatech.shipping.strategy.ShippingCalculationStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock RouteRepository routeRepository;
    @Mock ShipmentRepository shipmentRepository;
    @Mock RoutingApiService routingApiService;
    @Mock Map<String, ShippingCalculationStrategy> calculationStrategies;

    @InjectMocks RouteService routeService;

    private Shipment pendingShipment(String id) {
        return Shipment.builder()
                .id(id)
                .orderId("order-" + id)
                .shippingAddress("Calle Test 123")
                .deliveryStatus(DeliveryStatus.PENDING)
                .build();
    }

    @Test
    void createRoute_optimizeTrue_callsRoutingApiService() {
        Shipment s = pendingShipment("s1");
        when(shipmentRepository.findAllById(List.of("s1"))).thenReturn(List.of(s));
        when(routingApiService.fetchOptimizedPath(any(Route.class), anyList())).thenReturn("{\"source\":\"OSRM\"}");
        when(calculationStrategies.get(any())).thenReturn(null);
        when(routeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Route result = routeService.createRoute("company1", "LOCAL", "Av. Principal 1", List.of("s1"), true);

        assertThat(result.getOptimizedPathJson()).contains("OSRM");
        verify(routingApiService).fetchOptimizedPath(any(), anyList());
    }

    @Test
    void createRoute_optimizeFalse_skipsRoutingApiService() {
        Shipment s = pendingShipment("s1");
        when(shipmentRepository.findAllById(List.of("s1"))).thenReturn(List.of(s));
        when(calculationStrategies.get(any())).thenReturn(null);
        when(routeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Route result = routeService.createRoute("company1", "LOCAL", "Av. Principal 1", List.of("s1"), false);

        assertThat(result.getOptimizedPathJson()).contains("manual");
        verify(routingApiService, never()).fetchOptimizedPath(any(), anyList());
    }

    @Test
    void createRoute_shipmentAlreadyAssigned_throwsException() {
        Shipment assigned = Shipment.builder()
                .id("s1").orderId("o1").shippingAddress("Addr")
                .deliveryStatus(DeliveryStatus.ASSIGNED)
                .route(new Route())
                .build();
        when(shipmentRepository.findAllById(List.of("s1"))).thenReturn(List.of(assigned));

        assertThatThrownBy(() ->
                routeService.createRoute("c1", "LOCAL", "Addr", List.of("s1"), false))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createRoute_someIdsNotFound_throwsException() {
        Shipment s = pendingShipment("s1");
        when(shipmentRepository.findAllById(List.of("s1", "s2"))).thenReturn(List.of(s));

        assertThatThrownBy(() ->
                routeService.createRoute("c1", "LOCAL", "Addr", List.of("s1", "s2"), false))
                .isInstanceOf(com.donatech.shipping.exception.ShipmentNotFoundException.class);
    }

    @Test
    void getAllRoutes_noFilters_returnsAll() {
        when(routeRepository.findAll()).thenReturn(List.of(new Route()));

        List<Route> result = routeService.getAllRoutes(null, null);

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllRoutes_withCompanyAndStatus_returnsFiltered() {
        Route r = Route.builder().companyId("c1").status(RouteStatus.PLANNED).build();
        when(routeRepository.findByCompanyIdAndStatus("c1", RouteStatus.PLANNED)).thenReturn(List.of(r));

        List<Route> result = routeService.getAllRoutes("c1", RouteStatus.PLANNED);

        assertThat(result).hasSize(1);
    }

    @Test
    void getRouteById_exists_returnsRoute() {
        Route r = Route.builder().id("r1").status(RouteStatus.PLANNED).build();
        when(routeRepository.findById("r1")).thenReturn(Optional.of(r));

        Route result = routeService.getRouteById("r1");

        assertThat(result.getId()).isEqualTo("r1");
    }

    @Test
    void getRouteById_notFound_throwsException() {
        when(routeRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> routeService.getRouteById("missing"))
                .isInstanceOf(RouteNotFoundException.class);
    }

    @Test
    void updateRouteStatus_toInProgress_dispatchesAllShipments() {
        Shipment s = pendingShipment("s1");
        s.setDeliveryStatus(DeliveryStatus.ASSIGNED);
        Route r = Route.builder().id("r1").status(RouteStatus.PLANNED)
                .shipments(new ArrayList<>(List.of(s))).build();
        when(routeRepository.findById("r1")).thenReturn(Optional.of(r));
        when(routeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Route result = routeService.updateRouteStatus("r1", RouteStatus.IN_PROGRESS);

        assertThat(result.getStatus()).isEqualTo(RouteStatus.IN_PROGRESS);
        assertThat(s.getDeliveryStatus()).isEqualTo(DeliveryStatus.DISPATCHED);
        verify(shipmentRepository).save(s);
    }

    @Test
    void deleteRoute_cancelsRouteAndFreesShipments() {
        Shipment s = pendingShipment("s1");
        s.setDeliveryStatus(DeliveryStatus.ASSIGNED);
        Route r = Route.builder().id("r1").status(RouteStatus.PLANNED)
                .shipments(new ArrayList<>(List.of(s))).build();
        s.setRoute(r);

        when(routeRepository.findById("r1")).thenReturn(Optional.of(r));
        when(routeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        routeService.deleteRoute("r1");

        assertThat(r.getStatus()).isEqualTo(RouteStatus.CANCELLED);
        assertThat(s.getDeliveryStatus()).isEqualTo(DeliveryStatus.PENDING);
        assertThat(s.getRoute()).isNull();
    }
}
