package com.donatech.shipping.strategy;

import com.donatech.shipping.model.Route;
import com.donatech.shipping.model.Shipment;

import java.util.List;

public interface ShippingCalculationStrategy {
    String calculateRoute(Route route, List<Shipment> shipments, boolean optimizeRoute);
}
