package com.donatech.shipping.event;

import com.donatech.shipping.enums.DeliveryStatus;
import com.donatech.shipping.model.Shipment;
import com.donatech.shipping.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderReadyForShippingConsumer {

    private final ShipmentRepository shipmentRepository;

    @RabbitListener(queues = "shipping.order.ready")
    public void handleOrderReady(OrderReadyForShippingEvent event) {
        log.info("Creando shipment para orden id={}", event.orderId());
        Shipment shipment = Shipment.builder()
                .orderId(event.orderId().toString())
                .customerEmail(event.userEmail())
                .shippingAddress("Pendiente de asignación")
                .deliveryStatus(DeliveryStatus.PENDING)
                .build();
        shipmentRepository.save(shipment);
    }
}
