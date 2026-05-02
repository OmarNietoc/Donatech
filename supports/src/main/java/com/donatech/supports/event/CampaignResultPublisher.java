package com.donatech.supports.event;

import com.donatech.supports.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignResultPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(CampaignResultEvent event) {
        String routingKey = event.approved() ? "campaign.activated" : "campaign.rejected";
        log.info("Publicando {} para campaña id={}", routingKey, event.campaignId());
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, routingKey, event);
    }
}
