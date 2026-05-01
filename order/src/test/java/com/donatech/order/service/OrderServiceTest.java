package com.donatech.order.service;

import com.donatech.order.client.ProductClient;
import com.donatech.order.controller.response.OrderResponse;
import com.donatech.order.controller.response.UserResponseDto;
import com.donatech.order.dto.OrderDto;
import com.donatech.order.dto.OrderItemRequestDto;
import com.donatech.order.dto.ProductResponseDto;
import com.donatech.order.model.Coupon;
import com.donatech.order.model.Order;
import com.donatech.order.model.OrderItem;
import com.donatech.order.model.DonationStatus;
import com.donatech.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CouponService couponService;

    @Mock
    private UserValidatorService userValidatorService;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_WithValidPayload_ShouldPersistAndDisableCoupon() {
        // Arrange
        OrderDto dto = buildOrderDto("SAVE5");
        UserResponseDto user = new UserResponseDto(1L, "Ana", "ana@shop.com", null, 1);
        // cupón de 5 (Integer)
        Coupon coupon = Coupon.builder()
                .id(10L)
                .code("SAVE5")
                .discountAmount(5)
                .active(true)
                .build();

        when(userValidatorService.getUserByEmail(dto.getUserEmail())).thenReturn(user);
        when(couponService.getCouponByCode("SAVE5")).thenReturn(coupon);
        // producto con precio 10 (Integer)
        when(productClient.getProductById("SKU-1")).thenReturn(buildProduct("SKU-1", 10));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ResponseEntity<OrderResponse> response = orderService.createOrder(dto);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ana@shop.com", response.getBody().getUserEmail());
        // subtotal = 10, descuento = 5 -> finalPrice = 5
        assertEquals(5, response.getBody().getDiscountApplied());
        assertEquals(5, response.getBody().getFinalPrice());
        verify(orderRepository).save(any(Order.class));
        verify(couponService).updateCouponStatusByCode("SAVE5", false);
    }

    @Test
    void updateOrder_WhenCouponCodeMissing_ShouldReuseExistingCoupon() {
        Long orderId = 4L;

        // Cupón ya asociado a la orden existente
        Coupon existingCoupon = Coupon.builder()
                .id(3L)
                .code("KEEP")
                .discountAmount(200)   // por ejemplo $200 de descuento
                .active(true)
                .build();

        // Ítem existente en la orden (MANZANAS FUJI)
        OrderItem existingItem = OrderItem.builder()
                .id(1L)
                .productId("FR001")
                .productName("Manzanas Fuji")
                .productDescription("Crujientes y dulces, cultivadas en el Valle del Maule. Perfectas para meriendas saludables o como ingrediente en postres.")
                .unitPrice(1200)   // precio en Integer
                .quantity(1)
                .subtotal(1200)
                .build();

        // Orden existente con UNA sola línea en items, lista MUTABLE
        Order existingOrder = Order.builder()
                .id(orderId)
                .userEmail("stored@shop.com")
                .estado(DonationStatus.CONFIRMED)
                .coupon(existingCoupon)
                .finalPrice(1000)       // 1200 - 200 de descuento
                .discountApplied(200)
                .orderDate(LocalDateTime.of(2024, 1, 1, 10, 0))
                .items(new java.util.ArrayList<>(List.of(existingItem))) // 🔁 lista mutable con 1 item
                .build();
        existingItem.setOrder(existingOrder);

        // DTO de actualización SIN cupón (para que se reutilice el existente)
        OrderDto dto = new OrderDto();
        dto.setUserEmail("ana@shop.com");
        dto.setItems(List.of(new OrderItemRequestDto("FR001", 1)));
        dto.setCouponCode(null);

        UserResponseDto user = new UserResponseDto(9L, "Ana", dto.getUserEmail(), null, 1);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(userValidatorService.getUserByEmail(dto.getUserEmail())).thenReturn(user);
        when(productClient.getProductById("FR001")).thenReturn(buildProduct("FR001", 1200));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<OrderResponse> response = orderService.updateOrder(orderId, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(DonationStatus.CONFIRMED, response.getBody().getEstado());
        assertEquals("KEEP", response.getBody().getCoupon().getCode());
        assertEquals(200, response.getBody().getDiscountApplied());
        verify(couponService, never()).getCouponByCode(anyString());
        verify(couponService, never()).updateCouponStatusByCode(anyString(), anyBoolean());
        verify(orderRepository).save(any(Order.class));
    }


    @Test
    void updateDonationStatusById_ShouldPersistStatusChange() {
        Order order = Order.builder()
                .id(8L)
                .estado(DonationStatus.PENDING_PAYMENT)
                .build();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> response = orderService.updateDonationStatusById(order.getId(), DonationStatus.SHIPPED);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(DonationStatus.SHIPPED, order.getEstado());
        verify(orderRepository).save(order);
    }

    @Test
    void getAllOrders_ShouldReturnMappedResponses() {
        Coupon coupon = Coupon.builder()
                .id(1L)
                .code("ACTIVE")
                .discountAmount(2)
                .active(true)
                .build();
        OrderItem item = OrderItem.builder()
                .id(2L)
                .productId("SKU-1")
                .productName("Keyboard")
                .productDescription("Mechanical keyboard")
                .unitPrice(20)
                .quantity(1)
                .subtotal(20)
                .build();
        Order order = Order.builder()
                .id(5L)
                .userEmail("buyer@shop.com")
                .estado(DonationStatus.PENDING_PAYMENT)
                .coupon(coupon)
                .finalPrice(18)
                .discountApplied(2)
                .orderDate(LocalDateTime.now())
                .items(List.of(item))
                .build();
        item.setOrder(order);

        when(orderRepository.findAll()).thenReturn(List.of(order));

        ResponseEntity<List<OrderResponse>> response = orderService.getAllOrders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("buyer@shop.com", response.getBody().get(0).getUserEmail());
        assertEquals("ACTIVE", response.getBody().get(0).getCoupon().getCode());
        verify(orderRepository).findAll();
    }

    // Helpers

    private OrderDto buildOrderDto(String couponCode) {
        OrderDto dto = new OrderDto();
        dto.setUserEmail("ana@shop.com");
        dto.setItems(List.of(new OrderItemRequestDto("SKU-1", 1)));
        dto.setCouponCode(couponCode);
        return dto;
    }

    private ProductResponseDto buildProduct(String id, int price) {
        ProductResponseDto product = new ProductResponseDto();
        product.setId(id);
        product.setNombre("Sample");
        product.setDescripcion("Sample product");
        product.setPrecio(price);
        product.setImagen(new byte[]{1, 2, 3});
        return product;
    }
}
