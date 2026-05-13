package com.donatech.shipping.strategy;

import com.donatech.shipping.model.Route;
import com.donatech.shipping.model.Shipment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("dhlStrategy")
public class DhlStrategy implements ShippingCalculationStrategy {

    @Override
    public String calculateRoute(Route route, List<Shipment> shipments, boolean optimizeRoute) {
        return "{\"source\":\"DHL\",\"transport_mode\":\"air\",\"path\":[\"origin\",\"checkpoint_dhl_1\",\"destination\"]}";
    }
}
