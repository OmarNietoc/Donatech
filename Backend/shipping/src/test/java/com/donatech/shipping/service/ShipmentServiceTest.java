package com.donatech.shipping.service;

import com.donatech.shipping.enums.DeliveryStatus;
import com.donatech.shipping.event.OrderShippedEvent;
import com.donatech.shipping.event.ShippingEventPublisher;
import com.donatech.shipping.exception.ShipmentNotFoundException;
import com.donatech.shipping.model.Shipment;
import com.donatech.shipping.repository.RouteRepository;
import com.donatech.shipping.repository.ShipmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock ShipmentRepository shipmentRepository;
    @Mock RouteRepository routeRepository;
    @Mock ShippingEventPublisher shippingEventPublisher;

    @InjectMocks ShipmentService shipmentService;

    private Shipment buildShipment(String id, DeliveryStatus status) {
        return Shipment.builder()
                .id(id)
                .orderId("order-1")
                .customerEmail("client@test.cl")
                .shippingAddress("Av. Test 123")
                .trackingNumber("TRK-001")
                .deliveryStatus(status)
                .build();
    }

    @Test
    void getAllShipments_noFilter_returnsAll() {
        when(shipmentRepository.findAll()).thenReturn(List.of(buildShipment("s1", DeliveryStatus.PENDING)));

        List<Shipment> result = shipmentService.getAllShipments(null);

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllShipments_withStatusFilter_returnsFiltered() {
        Shipment s = buildShipment("s1", DeliveryStatus.DISPATCHED);
        when(shipmentRepository.findByDeliveryStatus(DeliveryStatus.DISPATCHED)).thenReturn(List.of(s));

        List<Shipment> result = shipmentService.getAllShipments(DeliveryStatus.DISPATCHED);

        assertThat(result).allMatch(x -> x.getDeliveryStatus() == DeliveryStatus.DISPATCHED);
    }

    @Test
    void getShipmentById_exists_returnsShipment() {
        Shipment s = buildShipment("abc", DeliveryStatus.PENDING);
        when(shipmentRepository.findById("abc")).thenReturn(Optional.of(s));

        Shipment result = shipmentService.getShipmentById("abc");

        assertThat(result.getId()).isEqualTo("abc");
    }

    @Test
    void getShipmentById_notFound_throwsException() {
        when(shipmentRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shipmentService.getShipmentById("missing"))
                .isInstanceOf(ShipmentNotFoundException.class);
    }

    @Test
    void getShipmentByTrackingNumber_exists_returnsShipment() {
        Shipment s = buildShipment("s2", DeliveryStatus.PENDING);
        s.setTrackingNumber("TRK-XYZ");
        when(shipmentRepository.findByTrackingNumber("TRK-XYZ")).thenReturn(Optional.of(s));

        Shipment result = shipmentService.getShipmentByTrackingNumber("TRK-XYZ");

        assertThat(result.getTrackingNumber()).isEqualTo("TRK-XYZ");
    }

    @Test
    void createShipment_nullStatus_setsPending() {
        Shipment incoming = Shipment.builder().orderId("o1").shippingAddress("Test").build();
        when(shipmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Shipment result = shipmentService.createShipment(incoming);

        assertThat(result.getDeliveryStatus()).isEqualTo(DeliveryStatus.PENDING);
    }

    @Test
    void updateShipmentStatus_validTransition_updatesStatus() {
        Shipment s = buildShipment("s1", DeliveryStatus.PENDING);
        when(shipmentRepository.findById("s1")).thenReturn(Optional.of(s));
        when(shipmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Shipment result = shipmentService.updateShipmentStatus("s1", DeliveryStatus.ASSIGNED);

        assertThat(result.getDeliveryStatus()).isEqualTo(DeliveryStatus.ASSIGNED);
    }

    @Test
    void updateShipmentStatus_toDispatched_publishesOrderShippedEvent() {
        Shipment s = buildShipment("s1", DeliveryStatus.ASSIGNED);
        s.setOrderId("42");
        when(shipmentRepository.findById("s1")).thenReturn(Optional.of(s));
        when(shipmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        shipmentService.updateShipmentStatus("s1", DeliveryStatus.DISPATCHED);

        verify(shippingEventPublisher).publishOrderShipped(any(OrderShippedEvent.class));
    }

    @Test
    void updateShipmentStatus_toDelivered_setsActualDelivery() {
        Shipment s = buildShipment("s1", DeliveryStatus.DISPATCHED);
        when(shipmentRepository.findById("s1")).thenReturn(Optional.of(s));
        when(shipmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Shipment result = shipmentService.updateShipmentStatus("s1", DeliveryStatus.DELIVERED);

        assertThat(result.getActualDelivery()).isNotNull();
    }

    @Test
    void updateShipmentStatus_invalidTransition_throwsException() {
        Shipment s = buildShipment("s1", DeliveryStatus.DELIVERED);
        when(shipmentRepository.findById("s1")).thenReturn(Optional.of(s));

        // DELIVERED(step=4) cannot go to ASSIGNED(step=2)
        assertThatThrownBy(() -> shipmentService.updateShipmentStatus("s1", DeliveryStatus.ASSIGNED))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deleteShipment_existingId_deletesFromRepository() {
        Shipment s = buildShipment("s1", DeliveryStatus.PENDING);
        when(shipmentRepository.findById("s1")).thenReturn(Optional.of(s));

        shipmentService.deleteShipment("s1");

        verify(shipmentRepository).delete(s);
    }
}
