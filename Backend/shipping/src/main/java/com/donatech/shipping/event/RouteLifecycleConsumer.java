package com.donatech.shipping.event;

import com.donatech.shipping.service.RouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * Sincroniza el estado de la ruta/envío con el ciclo de vida de la orden (order ms):
 *  - order.shipped     → shipment DISPATCHED + ruta IN_PROGRESS
 *  - order.delivered   → shipment DELIVERED  + cierre de ruta (COMPLETED/CANCELLED)
 *  - donation.cancelled→ shipment CANCELLED  + cierre de ruta
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RouteLifecycleConsumer {

    private final RouteService routeService;

    @RabbitListener(queues = "shipping.order.shipped")
    public void handleOrderShipped(OrderShippedEvent event) {
        log.info("shipping recibe order.shipped orden={}", event.orderId());
        routeService.onOrderShipped(event.orderId());
    }

    @RabbitListener(queues = "shipping.order.delivered")
    public void handleOrderDelivered(OrderDeliveredEvent event) {
        log.info("shipping recibe order.delivered orden={}", event.orderId());
        routeService.onOrderDelivered(event.orderId());
    }

    @RabbitListener(queues = "shipping.donation.cancelled")
    public void handleDonationCancelled(DonationCancelledEvent event) {
        log.info("shipping recibe donation.cancelled orden={}", event.donationId());
        routeService.onDonationCancelled(event.donationId());
    }
}
