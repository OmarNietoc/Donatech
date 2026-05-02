package com.donatech.notification.consumer;

import com.donatech.notification.event.OrderShippedEvent;
import com.donatech.notification.event.TransferResultEvent;
import com.donatech.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = "notification.transfer.rejected")
    public void handleTransferRejected(TransferResultEvent event) {
        log.info("Notificando transferencia rechazada para orden id={}", event.orderId());
        // TODO: resolver email del donante desde orderId (via Feign a order ms)
        log.info("Transferencia orden #{} rechazada. Motivo: {}", event.orderId(), event.motivo());
    }

    @RabbitListener(queues = "notification.order.shipped")
    public void handleOrderShipped(OrderShippedEvent event) {
        log.info("Notificando envío de orden id={} a {}", event.orderId(), event.recipientEmail());
        emailService.sendEmail(
                event.recipientEmail(),
                "Tu donación está en camino — Donatech",
                "Tu donación (orden #" + event.orderId() + ") está en camino.\n" +
                "Seguimiento: " + event.trackingInfo()
        );
    }
}
