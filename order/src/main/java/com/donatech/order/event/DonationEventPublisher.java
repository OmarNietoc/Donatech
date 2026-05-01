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
}
