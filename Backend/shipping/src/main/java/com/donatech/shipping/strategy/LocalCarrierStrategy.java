package com.donatech.shipping.strategy;

import com.donatech.shipping.model.Route;
import com.donatech.shipping.model.Shipment;
import com.donatech.shipping.service.RoutingApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component("localCarrierStrategy")
@RequiredArgsConstructor
public class LocalCarrierStrategy implements ShippingCalculationStrategy {

    private final RoutingApiService routingApiService;
    private final ObjectMapper objectMapper;

    @Override
    public String calculateRoute(Route route, List<Shipment> shipments, boolean optimizeRoute) {
        if (optimizeRoute) {
            log.info("LocalCarrier: solicitando optimización OSRM para {} envíos", shipments.size());
            return routingApiService.fetchOptimizedPath(route, shipments);
        }
        return buildManualRouteJson(shipments);
    }

    private String buildManualRouteJson(List<Shipment> shipments) {
        try {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("source", "manual");
            result.put("total_stops", shipments.size());
            result.put("optimized_order", shipments.stream().map(Shipment::getId).toList());
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return "{\"source\":\"manual\"}";
        }
    }
}
