package com.donatech.shipping.event;

import com.donatech.shipping.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderShipped(OrderShippedEvent event) {
        log.info("Publicando order.shipped para orden id={}", event.orderId());
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "order.shipped", event);
    }
}
