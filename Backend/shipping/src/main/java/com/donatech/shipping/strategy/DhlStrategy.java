package com.donatech.shipping.strategy;

import com.donatech.shipping.model.Route;
import org.springframework.stereotype.Component;

@Component("dhlStrategy")
public class DhlStrategy implements ShippingCalculationStrategy {

    @Override
    public String calculateRoute(Route route) {
        // Simulación lógica de cálculo usando algoritmos propios o APIs de DHL
        return "{\"path\": [\"origin\", \"checkpoint_dhl_1\", \"destination\"], \"transport_mode\": \"air\"}";
    }
}

