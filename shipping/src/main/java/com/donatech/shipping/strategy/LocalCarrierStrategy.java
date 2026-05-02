package com.donatech.shipping.strategy;

import com.donatech.shipping.model.Route;
import org.springframework.stereotype.Component;

@Component("localCarrierStrategy")
public class LocalCarrierStrategy implements ShippingCalculationStrategy {

    @Override
    public String calculateRoute(Route route) {
        // Simulación lógica de cálculo usando rutas terrestres locales
        return "{\"path\": [\"origin\", \"local_depot\", \"destination\"], \"transport_mode\": \"ground\"}";
    }
}

