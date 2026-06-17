package com.donatech.shipping.repository;

import com.donatech.shipping.enums.DeliveryStatus;
import com.donatech.shipping.model.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, String> {
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    Optional<Shipment> findByOrderId(String orderId);
    List<Shipment> findByRouteId(String routeId);
    List<Shipment> findByDeliveryStatus(DeliveryStatus deliveryStatus);
}

