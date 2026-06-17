package com.donatech.order.event;

import com.donatech.order.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DonationEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishDonationConfirmed(DonationConfirmedEvent event) {
        log.info("Publicando donation.confirmed para donación id={}", event.donationId());
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "donation.confirmed", event);
    }

    public void publishTransferSubmitted(TransferSubmittedEvent event) {
        log.info("Publicando transfer.submitted para donación id={}", event.donationId());
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "transfer.submitted", event);
    }

    public void publishOrderReadyForShipping(OrderReadyForShippingEvent event) {
        log.info("Publicando order.ready_for_shipping para orden id={}", event.orderId());
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "order.ready_for_shipping", event);
    }

    public void publishDeliverySubmitted(DeliverySubmittedEvent event) {
        log.info("Publicando delivery.submitted para orden id={}", event.orderId());
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "delivery.submitted", event);
    }

    public void publishOrderDelivered(OrderDeliveredEvent event) {
        log.info("Publicando order.delivered para orden id={}", event.orderId());
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "order.delivered", event);
    }

    public void publishDonationCancelled(DonationCancelledEvent event) {
        log.info("Publicando donation.cancelled para donación id={}", event.donationId());
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "donation.cancelled", event);
    }

    public void publishBeneficiaryThankYou(BeneficiaryThankYouEvent event) {
        log.info("Publicando beneficiary.thank-you para orden id={}", event.orderId());
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "beneficiary.thank-you", event);
    }

    public void publishOrderShipped(OrderShippedEvent event) {
        log.info("Publicando order.shipped para orden id={}", event.orderId());
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "order.shipped", event);
    }

    public void publishDeliveryIncoming(BeneficiaryShipmentEvent event) {
        log.info("Publicando delivery.incoming para orden id={}", event.orderId());
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "delivery.incoming", event);
    }

    public void publishDeliveryConfirmRequest(DeliveryConfirmRequestEvent event) {
        log.info("Publicando delivery.confirm-request para orden id={}", event.orderId());
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "delivery.confirm-request", event);
    }
}
