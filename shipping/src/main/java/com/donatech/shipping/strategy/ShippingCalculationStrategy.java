package com.donatech.shipping.strategy;

import com.donatech.shipping.model.Route;

public interface ShippingCalculationStrategy {
    String calculateRoute(Route route);
}

