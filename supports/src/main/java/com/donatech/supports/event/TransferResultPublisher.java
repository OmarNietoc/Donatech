package com.donatech.supports.event;

import com.donatech.supports.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferResultPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(TransferResultEvent event) {
        String routingKey = event.approved() ? "transfer.validated" : "transfer.rejected";
        log.info("Publicando {} para orden id={}", routingKey, event.orderId());
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, routingKey, event);
    }
}
