package com.donatech.shipping.service;

import com.donatech.shipping.model.Route;
import com.donatech.shipping.model.Shipment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RoutingApiServiceTest {

    @Mock com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @InjectMocks RoutingApiService routingApiService;

    @BeforeEach
    void setUp() throws Exception {
        ReflectionTestUtils.setField(routingApiService, "osrmBaseUrl", "http://router.project-osrm.org");
        ReflectionTestUtils.setField(routingApiService, "nominatimBaseUrl", "https://nominatim.openstreetmap.org");
        // Use real ObjectMapper for JSON serialization
        ReflectionTestUtils.setField(routingApiService, "objectMapper",
                new com.fasterxml.jackson.databind.ObjectMapper());
        // Initialize RestClient manually (simulates @PostConstruct)
        routingApiService.init();
    }

    @Test
    void fetchOptimizedPath_noCoords_returnsFallbackJson() {
        Route route = Route.builder().originAddress("Av. Test 123, Santiago").build();
        Shipment s = Shipment.builder().id("s1").shippingAddress("Otra Calle").build();
        // No latitude/longitude set → all shipments filtered out

        String result = routingApiService.fetchOptimizedPath(route, List.of(s));

        assertThat(result).contains("fallback");
    }

    @Test
    void fallbackCalculateRoute_returnsValidFallbackJson() {
        Route route = Route.builder().build();
        Shipment s = Shipment.builder().id("s1").build();

        String result = routingApiService.fallbackCalculateRoute(route, List.of(s), new RuntimeException("CB open"));

        assertThat(result).contains("fallback");
        assertThat(result).contains("total_stops");
    }

    @Test
    void fetchOptimizedPath_emptyShipmentList_returnsFallback() {
        Route route = Route.builder().originAddress("Santiago").build();

        String result = routingApiService.fetchOptimizedPath(route, List.of());

        assertThat(result).contains("fallback");
    }
}
