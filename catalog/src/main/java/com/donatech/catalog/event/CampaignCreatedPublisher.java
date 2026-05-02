package com.donatech.catalog.event;

import com.donatech.catalog.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CampaignCreatedPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishCampaignCreated(CampaignCreatedEvent event) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "campaign.created", event);
    }
}
