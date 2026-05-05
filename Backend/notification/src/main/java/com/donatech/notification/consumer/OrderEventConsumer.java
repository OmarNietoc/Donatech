package com.donatech.notification.consumer;

import com.donatech.notification.event.OrderShippedEvent;
import com.donatech.notification.event.TransferResultEvent;
import com.donatech.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = "notification.transfer.rejected")
    public void handleTransferRejected(TransferResultEvent event) {
        log.info("Notificando transferencia rechazada para orden id={}", event.orderId());
        Context ctx = new Context();
        ctx.setVariable("orderId", event.orderId());
        ctx.setVariable("motivo", event.motivo());
        emailService.sendHtmlEmail(
                event.recipientEmail(),
                "Tu transferencia no fue aprobada — Donatech",
                "transfer-rejected",
                ctx
        );
    }

    @RabbitListener(queues = "notification.order.shipped")
    public void handleOrderShipped(OrderShippedEvent event) {
        log.info("Notificando envío de orden id={} a {}", event.orderId(), event.recipientEmail());
        Context ctx = new Context();
        ctx.setVariable("orderId", event.orderId());
        ctx.setVariable("trackingInfo", event.trackingInfo());
        emailService.sendHtmlEmail(
                event.recipientEmail(),
                "Tu donación está en camino — Donatech",
                "order-shipped",
                ctx
        );
    }
}
