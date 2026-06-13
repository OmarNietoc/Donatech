package com.donatech.catalog.event;

import com.donatech.catalog.repository.CampaignKitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderDeliveredConsumer {

    private final CampaignKitRepository campaignKitRepository;

    // Incrementa el contador "entregados" por kit en la campaña al confirmarse la entrega.
    @Transactional
    @RabbitListener(queues = "catalog.order.delivered")
    public void handleOrderDelivered(OrderDeliveredEvent event) {
        log.info("Registrando entrega para orden id={} en campaña={}", event.orderId(), event.campaignId());
        if (event.campaignId() == null || event.items() == null) return;
        event.items().forEach(item ->
                campaignKitRepository.findByCampaignIdAndKitId(event.campaignId(), item.kitId()).ifPresentOrElse(ck -> {
                    ck.setCantidadEntregada(ck.getCantidadEntregada() + item.quantity());
                    campaignKitRepository.save(ck);
                }, () -> log.warn("CampaignKit no encontrado para campaña={} kit={}; contador entregados no actualizado",
                        event.campaignId(), item.kitId())));
    }
}
