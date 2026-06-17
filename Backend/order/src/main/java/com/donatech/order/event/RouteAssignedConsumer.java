package com.donatech.order.event;

import com.donatech.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteAssignedConsumer {

    private final OrderService orderService;

    @RabbitListener(queues = "order.route.assigned")
    public void handleRouteAssigned(RouteAssignedEvent event) {
        if (event.orderIds() == null) return;
        for (Long orderId : event.orderIds()) {
            try {
                orderService.assignFromRoute(orderId, event.routeId(), event.routeName(), event.collaboratorId(),
                        event.collaboratorNombre(), event.collaboratorEmail());
            } catch (Exception e) {
                log.warn("No se pudo asignar la orden {} a la ruta {}: {}",
                        orderId, event.routeId(), e.getMessage());
            }
        }
    }
}
