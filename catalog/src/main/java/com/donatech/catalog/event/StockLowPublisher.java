package com.donatech.catalog.event;

import com.donatech.catalog.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockLowPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishStockLow(StockLowEvent event) {
        log.warn("Publicando stock.low para producto id={}, stock={}", event.productId(), event.currentStock());
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "stock.low", event);
    }
}
