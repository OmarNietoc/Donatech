package com.donatech.shipping.service;

import com.donatech.shipping.enums.DeliveryStatus;
import com.donatech.shipping.event.OrderShippedEvent;
import com.donatech.shipping.event.ShippingEventPublisher;
import com.donatech.shipping.exception.ShipmentNotFoundException;
import com.donatech.shipping.model.Shipment;
import com.donatech.shipping.repository.RouteRepository;
import com.donatech.shipping.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final RouteRepository routeRepository;
    private final ShippingEventPublisher shippingEventPublisher;

    public List<Shipment> getAllShipments(DeliveryStatus deliveryStatus) {
        if (deliveryStatus != null) {
            return shipmentRepository.findByDeliveryStatus(deliveryStatus);
        }
        return shipmentRepository.findAll();
    }

    public Shipment getShipmentByTrackingNumber(String trackingNumber) {
        return shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ShipmentNotFoundException("El envío con número de seguimiento " + trackingNumber + " no fue encontrado."));
    }

    public Shipment getShipmentById(String id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new ShipmentNotFoundException("El envío con ID " + id + " no fue encontrado."));
    }

    @Transactional
    public Shipment createShipment(Shipment shipment) {
        // En caso de que se envíe asociado desde el json, el framework lo intenta poblar.
        // Se asume estado inicial "PENDING" si no se establece.
        if (shipment.getDeliveryStatus() == null) {
            shipment.setDeliveryStatus(DeliveryStatus.PENDING);
        }
        return shipmentRepository.save(shipment);
    }

    @Transactional
    public Shipment updateShipmentStatus(String id, DeliveryStatus newStatus) {
        Shipment existing = getShipmentById(id);
        if (!existing.getDeliveryStatus().canTransitionTo(newStatus)) {
            throw new IllegalStateException("Transición de estado no válida de " + existing.getDeliveryStatus() + " a " + newStatus);
        }
        existing.setDeliveryStatus(newStatus);
        if (newStatus == DeliveryStatus.DELIVERED) {
            existing.setActualDelivery(java.time.LocalDateTime.now());
        }
        if (newStatus == DeliveryStatus.DISPATCHED) {
            shippingEventPublisher.publishOrderShipped(
                    new OrderShippedEvent(
                            Long.parseLong(existing.getOrderId()),
                            existing.getCustomerEmail(),
                            existing.getTrackingNumber()
                    )
            );
        }
        return shipmentRepository.save(existing);
    }

    @Transactional
    public void deleteShipment(String id) {
        Shipment existing = getShipmentById(id);
        shipmentRepository.delete(existing);
    }
}

