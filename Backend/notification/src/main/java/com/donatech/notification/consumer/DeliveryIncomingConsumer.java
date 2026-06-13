package com.donatech.notification.consumer;

import com.donatech.notification.event.BeneficiaryShipmentEvent;
import com.donatech.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryIncomingConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = "notification.delivery.incoming")
    public void handleDeliveryIncoming(BeneficiaryShipmentEvent event) {
        log.info("Notificando entrega en camino al beneficiario {} (orden id={})",
                event.beneficiaryEmail(), event.orderId());

        Context ctx = new Context();
        ctx.setVariable("orderId", event.orderId());
        ctx.setVariable("beneficiaryName", event.beneficiaryName() != null ? event.beneficiaryName() : "beneficiario");
        ctx.setVariable("kitNames", event.kitNames());
        ctx.setVariable("direccion", event.direccion());
        ctx.setVariable("telefono", event.telefono());
        ctx.setVariable("supportLink", event.supportLink());
        ctx.setVariable("fechaEstimada", event.fechaEstimada());

        emailService.sendHtmlEmail(
                event.beneficiaryEmail(),
                "📦 Tu ayuda está en camino — Donatech",
                "delivery-incoming",
                ctx
        );
    }
}
