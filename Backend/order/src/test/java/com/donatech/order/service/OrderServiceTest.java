package com.donatech.order.service;

import com.donatech.order.client.KitClient;
import com.donatech.order.controller.response.MessageResponse;
import com.donatech.order.controller.response.OrderResponse;
import com.donatech.order.controller.response.UserResponseDto;
import com.donatech.order.dto.KitResponseDto;
import com.donatech.order.dto.OrderDto;
import com.donatech.order.dto.OrderItemRequestDto;
import com.donatech.order.event.DonationEventPublisher;
import com.donatech.order.exception.ResourceNotFoundException;
import com.donatech.order.model.Coupon;
import com.donatech.order.model.DonationStatus;
import com.donatech.order.model.Order;
import com.donatech.order.model.TrackingHistory;
import com.donatech.order.repository.OrderRepository;
import com.donatech.order.repository.TrackingHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock TrackingHistoryRepository trackingHistoryRepository;
    @Mock CouponService couponService;
    @Mock UserValidatorService userValidatorService;
    @Mock KitClient kitClient;
    @Mock DonationEventPublisher donationEventPublisher;

    @InjectMocks OrderService orderService;

    private UserResponseDto user(String email) {
        return new UserResponseDto(1L, "Test User", email, null, 1);
    }

    private KitResponseDto kit(Long id, int precio) {
        KitResponseDto k = new KitResponseDto();
        k.setId(id);
        k.setNombre("Kit Test");
        k.setPrecioEstimado(precio);
        return k;
    }

    private Order baseOrder(Long id, DonationStatus status) {
        return Order.builder()
                .id(id)
                .userEmail("donor@test.cl")
                .estado(status)
                .finalPrice(0)
                .discountApplied(0)
                .orderDate(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();
    }

    @Test
    void createOrder_validDto_savesOrder() {
        OrderDto dto = new OrderDto();
        dto.setUserEmail("donor@test.cl");
        dto.setItems(List.of(new OrderItemRequestDto(1L, 2)));

        when(userValidatorService.getUserByEmail("donor@test.cl")).thenReturn(user("donor@test.cl"));
        when(kitClient.getKitById(1L)).thenReturn(kit(1L, 3000));
        when(orderRepository.save(any())).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });

        ResponseEntity<OrderResponse> response = orderService.createOrder(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getFinalPrice()).isEqualTo(6000);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_withCoupon_appliesDiscount() {
        Coupon coupon = Coupon.builder().code("SAVE10").discountAmount(1000).active(true).build();
        OrderDto dto = new OrderDto();
        dto.setUserEmail("donor@test.cl");
        dto.setItems(List.of(new OrderItemRequestDto(1L, 1)));
        dto.setCouponCode("SAVE10");

        when(userValidatorService.getUserByEmail("donor@test.cl")).thenReturn(user("donor@test.cl"));
        when(kitClient.getKitById(1L)).thenReturn(kit(1L, 5000));
        when(couponService.getCouponByCode("SAVE10")).thenReturn(coupon);
        when(orderRepository.save(any())).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(2L);
            return o;
        });

        ResponseEntity<OrderResponse> response = orderService.createOrder(dto);

        assertThat(response.getBody().getFinalPrice()).isEqualTo(4000);
        assertThat(response.getBody().getDiscountApplied()).isEqualTo(1000);
        verify(couponService).updateCouponStatusByCode("SAVE10", false);
    }

    @Test
    void getOrderById_exists_returnsOrder() {
        Order order = baseOrder(1L, DonationStatus.DRAFT);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order result = orderService.getOrderById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getOrderById_notFound_throwsException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateDonationStatusById_validTransition_updatesAndCreatesHistory() {
        Order order = baseOrder(1L, DonationStatus.EN_VALIDACION_TRANSFERENCIA);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);
        when(trackingHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<MessageResponse> response = orderService.updateDonationStatusById(1L, DonationStatus.EN_PREPARACION, 10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(order.getEstado()).isEqualTo(DonationStatus.EN_PREPARACION);
        verify(trackingHistoryRepository).save(any(TrackingHistory.class));
        // donation.confirmed solo se publica desde TransferValidatedConsumer
        verify(donationEventPublisher, never()).publishDonationConfirmed(any());
    }

    @Test
    void updateDonationStatusById_invalidTransition_throwsException() {
        Order order = baseOrder(1L, DonationStatus.ENTREGADA);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateDonationStatusById(1L, DonationStatus.DRAFT, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transición de estado no permitida");
    }

    @Test
    void uploadTransferProof_storesBytesAndSetsEnValidacion() {
        Order order = baseOrder(1L, DonationStatus.INGRESADA);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        byte[] proof = "comprobante".getBytes();
        ResponseEntity<MessageResponse> response = orderService.uploadTransferProof(1L, proof);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(order.getEstado()).isEqualTo(DonationStatus.EN_VALIDACION_TRANSFERENCIA);
        assertThat(order.getTransferProof()).isEqualTo(proof);
        verify(donationEventPublisher).publishTransferSubmitted(any());
    }

    @Test
    void cancelOrder_afterStockDeducted_publishesDonationCancelled() {
        Order order = baseOrder(1L, DonationStatus.EN_PREPARACION);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        ResponseEntity<MessageResponse> response = orderService.cancelOrder(1L, "ya no puedo donar", 5L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(order.getEstado()).isEqualTo(DonationStatus.CANCELADA);
        assertThat(order.getRejectionReason()).isEqualTo("ya no puedo donar");
        verify(donationEventPublisher).publishDonationCancelled(any());
    }

    @Test
    void cancelOrder_beforeStockDeducted_doesNotPublishRestore() {
        Order order = baseOrder(1L, DonationStatus.INGRESADA);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        orderService.cancelOrder(1L, null, null);

        assertThat(order.getEstado()).isEqualTo(DonationStatus.CANCELADA);
        verify(donationEventPublisher, never()).publishDonationCancelled(any());
    }

    @Test
    void confirmDelivery_setsEntregadaWithConfirmationData() {
        Order order = baseOrder(1L, DonationStatus.PENDIENTE_CONFIRMACION);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        ResponseEntity<MessageResponse> response = orderService.confirmDelivery(1L, 99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(order.getEstado()).isEqualTo(DonationStatus.ENTREGADA);
        assertThat(order.getDeliveryConfirmedBy()).isEqualTo(99L);
        assertThat(order.getDeliveryConfirmedAt()).isNotNull();
    }

    @Test
    void getAllOrders_returnsMappedList() {
        when(orderRepository.findAll()).thenReturn(List.of(
                baseOrder(1L, DonationStatus.DRAFT),
                baseOrder(2L, DonationStatus.INGRESADA)
        ));

        ResponseEntity<List<OrderResponse>> response = orderService.getAllOrders();

        assertThat(response.getBody()).hasSize(2);
    }
}
