package com.donatech.shipping.event;

import com.donatech.shipping.client.ContactDto;
import com.donatech.shipping.client.UsersClient;
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
    private final UsersClient usersClient;

    @RabbitListener(queues = "shipping.order.ready")
    public void handleOrderReady(OrderReadyForShippingEvent event) {
        log.info("Creando shipment para orden id={}", event.orderId());

        // Idempotencia: si el mensaje se reprocesa (requeue/retry), no crear duplicado.
        if (shipmentRepository.findByOrderId(event.orderId().toString()).isPresent()) {
            log.warn("Ya existe shipment para orden id={}, se omite duplicado", event.orderId());
            return;
        }

        // Enriquecer con datos del beneficiario (nombre, dirección, teléfono) desde users ms.
        String nombre = null;
        String direccion = "Pendiente de asignación";
        String phone = null;
        if (event.beneficiaryId() != null) {
            try {
                ContactDto c = usersClient.getContact(event.beneficiaryId());
                if (c != null) {
                    nombre = c.name();
                    phone = c.phone();
                    if (c.direccion() != null && !c.direccion().isBlank()) {
                        direccion = c.direccion();
                    }
                }
            } catch (Exception e) {
                log.warn("No se pudo obtener contacto del beneficiario {}: {}", event.beneficiaryId(), e.getMessage());
            }
        }

        Shipment shipment = Shipment.builder()
                .orderId(event.orderId().toString())
                .customerEmail(event.userEmail())
                .customerName(nombre)
                .phone(phone)
                .beneficiaryId(event.beneficiaryId())
                .shippingAddress(direccion)
                .deliveryStatus(DeliveryStatus.PENDING)
                .build();
        shipmentRepository.save(shipment);
    }
}
