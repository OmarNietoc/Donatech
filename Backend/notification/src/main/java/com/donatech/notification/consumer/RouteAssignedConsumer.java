package com.donatech.notification.consumer;

import com.donatech.notification.event.RouteAssignedEvent;
import com.donatech.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouteAssignedConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = "notification.route.assigned")
    public void handleRouteAssigned(RouteAssignedEvent event) {
        if (event.collaboratorEmail() == null || event.collaboratorEmail().isBlank()) {
            log.warn("route.assigned sin email de colaborador (ruta {})", event.routeId());
            return;
        }
        int total = event.orderIds() != null ? event.orderIds().size() : 0;
        log.info("Notificando ruta asignada a colaborador {} ({} entregas)", event.collaboratorEmail(), total);

        Context ctx = new Context();
        ctx.setVariable("nombre", event.collaboratorNombre() != null ? event.collaboratorNombre() : "colaborador");
        ctx.setVariable("totalEntregas", total);

        emailService.sendHtmlEmail(
                event.collaboratorEmail(),
                "🚚 Se te asignó una ruta de entregas — Donatech",
                "route-assigned",
                ctx
        );
    }
}
