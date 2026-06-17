package com.donatech.notification.consumer;

import com.donatech.notification.event.DeliveryConfirmRequestEvent;
import com.donatech.notification.event.DeliverySubmittedEvent;
import com.donatech.notification.event.OrderDeliveredEvent;
import com.donatech.notification.event.OrderShippedEvent;
import com.donatech.notification.event.TransferResultEvent;
import com.donatech.notification.event.TransferSubmittedEvent;
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
        log.info("Notificando transferencia rechazada para donación id={}", event.donationId());
        Context ctx = new Context();
        ctx.setVariable("orderId", event.donationId());
        ctx.setVariable("donationId", event.donationId());
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

    @RabbitListener(queues = "notification.donation.received")
    public void handleDonationReceived(TransferSubmittedEvent event) {
        log.info("Notificando donación recibida para donación id={} a {}", event.donationId(), event.userEmail());
        Context ctx = new Context();
        ctx.setVariable("orderId", event.donationId());
        ctx.setVariable("donationId", event.donationId());
        emailService.sendHtmlEmail(
                event.userEmail(),
                "Tu donación fue recibida — Donatech",
                "donation-received-donor",
                ctx
        );
    }

    @RabbitListener(queues = "notification.transfer.approved")
    public void handleTransferApproved(TransferResultEvent event) {
        if (!event.approved()) return;
        log.info("Notificando transferencia aprobada para donación id={} a {}", event.donationId(), event.recipientEmail());
        Context ctx = new Context();
        ctx.setVariable("orderId", event.donationId());
        ctx.setVariable("donationId", event.donationId());
        emailService.sendHtmlEmail(
                event.recipientEmail(),
                "Tu transferencia fue aprobada — Donatech",
                "transfer-approved",
                ctx
        );
    }

    @RabbitListener(queues = "notification.delivery.submitted")
    public void handleDeliverySubmitted(DeliverySubmittedEvent event) {
        log.info("Notificando evidencia de entrega para orden id={} a {}", event.orderId(), event.userEmail());
        Context ctx = new Context();
        ctx.setVariable("orderId", event.orderId());
        emailService.sendHtmlEmail(
                event.userEmail(),
                "Tu donación fue entregada — pendiente de confirmación — Donatech",
                "delivery-submitted",
                ctx
        );
    }

    @RabbitListener(queues = "notification.delivery.confirm-request")
    public void handleDeliveryConfirmRequest(DeliveryConfirmRequestEvent event) {
        log.info("Solicitando confirmación de entrega al beneficiario {} (orden id={})",
                event.beneficiaryEmail(), event.orderId());
        Context ctx = new Context();
        ctx.setVariable("orderId", event.orderId());
        ctx.setVariable("beneficiaryName", event.beneficiaryName() != null ? event.beneficiaryName() : "beneficiario");
        ctx.setVariable("kitNames", event.kitNames());
        ctx.setVariable("confirmLink", event.confirmLink());
        ctx.setVariable("supportLink", event.supportLink());
        emailService.sendHtmlEmail(
                event.beneficiaryEmail(),
                "Confirma la recepción de tu donación — Donatech",
                "delivery-confirm-beneficiary",
                ctx
        );
    }

    @RabbitListener(queues = "notification.order.delivered")
    public void handleOrderDelivered(OrderDeliveredEvent event) {
        log.info("Notificando entrega confirmada para orden id={} a {}", event.orderId(), event.userEmail());
        Context ctx = new Context();
        ctx.setVariable("orderId", event.orderId());
        emailService.sendHtmlEmail(
                event.userEmail(),
                "¡Tu donación llegó a destino! — Donatech",
                "order-delivered",
                ctx
        );
    }
}
