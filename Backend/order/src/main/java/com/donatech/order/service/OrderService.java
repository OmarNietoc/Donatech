package com.donatech.order.service;

import com.donatech.order.client.KitClient;
import com.donatech.order.controller.response.CouponResponse;
import com.donatech.order.controller.response.DashboardResponse;
import com.donatech.order.controller.response.MessageResponse;
import com.donatech.order.controller.response.OrderResponse;
import com.donatech.order.controller.response.UserResponseDto;
import com.donatech.order.dto.AddItemToOrderRequest;
import com.donatech.order.dto.KitResponseDto;
import com.donatech.order.dto.OrderDto;
import com.donatech.order.dto.OrderItemRequestDto;
import com.donatech.order.event.DonationConfirmedEvent;
import com.donatech.order.event.DonationEventPublisher;
import com.donatech.order.event.DonationItemEvent;
import com.donatech.order.event.TransferSubmittedEvent;
import com.donatech.order.exception.ResourceNotFoundException;
import com.donatech.order.model.Coupon;
import com.donatech.order.model.Order;
import com.donatech.order.model.OrderItem;
import com.donatech.order.model.DonationStatus;
import com.donatech.order.model.TrackingHistory;
import com.donatech.order.repository.OrderRepository;
import com.donatech.order.repository.TrackingHistoryRepository;
import feign.FeignException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final int ZERO = 0;

    private final OrderRepository orderRepository;
    private final TrackingHistoryRepository trackingHistoryRepository;
    private final CouponService couponService;
    private final UserValidatorService userValidatorService;
    private final KitClient kitClient;
    private final DonationEventPublisher donationEventPublisher;

    public ResponseEntity<OrderResponse> getOrderDtoById(Long id) {
        Order order = getOrderById(id);
        return ResponseEntity.ok(convertToDTO(order));
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada: " + id));
    }

    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> responses = orderRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // =========================
    // Obtener carrito activo
    // =========================
    public ResponseEntity<OrderResponse> getActiveOrderByUserEmail(String userEmail) {
        UserResponseDto user = userValidatorService.getUserByEmail(userEmail);

        Order order = orderRepository
                .findFirstByUserEmailAndEstadoOrderByOrderDateDesc(user.getEmail(), DonationStatus.DRAFT)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una orden pendiente para el usuario: " + user.getEmail()
                        )
                );

        return ResponseEntity.ok(convertToDTO(order));
    }

    // =========================
    // Agregar ítem al carrito
    // =========================
    public ResponseEntity<OrderResponse> addItemToCart(@Valid AddItemToOrderRequest request) {
        // Validar usuario
        UserResponseDto user = userValidatorService.getUserByEmail(request.getUserEmail());

        // Buscar orden PENDING existente
        Order order = orderRepository
                .findFirstByUserEmailAndEstadoOrderByOrderDateDesc(user.getEmail(), DonationStatus.DRAFT)
                .orElse(null);

        OrderItemRequestDto itemDto = request.getItem();

        // Si NO hay carrito -> crear uno nuevo con ese único ítem
        if (order == null) {
            OrderItem newItem = buildOrderItem(itemDto);

            Order newOrder = Order.builder()
                    .userEmail(user.getEmail())
                    .estado(DonationStatus.DRAFT)
                    .coupon(null)
                    .discountApplied(ZERO)
                    .orderDate(LocalDateTime.now())
                    .finalPrice(newItem.getSubtotal())
                    .campaignId(request.getCampaignId())
                    .items(new java.util.ArrayList<>())
                    .build();

            newItem.setOrder(newOrder);
            newOrder.getItems().add(newItem);

            orderRepository.save(newOrder);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(newOrder));
        }

        // Si SÍ hay carrito -> agregar o actualizar ítem
        OrderItem existingItem = order.getItems().stream()
                .filter(i -> i.getKitId().equals(itemDto.getKitId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Aumentar cantidad del producto ya existente
            int nuevaCantidad = existingItem.getQuantity() + itemDto.getQuantity();
            existingItem.setQuantity(nuevaCantidad);

            int newSubtotal = existingItem.getUnitPrice() * nuevaCantidad;
            existingItem.setSubtotal(newSubtotal);
        } else {
            // Agregar nuevo producto al carrito
            OrderItem newItem = buildOrderItem(itemDto);
            newItem.setOrder(order);
            order.getItems().add(newItem);
        }

        // Recalcular subtotal total (int)
        int subtotal = order.getItems().stream()
                .mapToInt(OrderItem::getSubtotal)
                .sum();

        // Mantener misma lógica de descuento de la orden
        Coupon coupon = order.getCoupon();
        int desiredDiscount = (coupon != null && coupon.getDiscountAmount() != null)
                ? coupon.getDiscountAmount()
                : ZERO;

        int discountApplied = Math.min(desiredDiscount, subtotal);
        int finalPrice = subtotal - discountApplied;

        order.setDiscountApplied(discountApplied);
        order.setFinalPrice(finalPrice);

        orderRepository.save(order);
        return ResponseEntity.ok(convertToDTO(order));
    }

    // =========================
    // Crear orden completa
    // =========================
    public ResponseEntity<OrderResponse> createOrder(@Valid OrderDto dto) {
        Order order = buildOrderFromDto(dto, null);
        orderRepository.save(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(order));
    }

    // =========================
    // Actualizar orden completa
    // =========================
    public ResponseEntity<OrderResponse> updateOrder(Long id, @Valid OrderDto dto) {
        Order existing = getOrderById(id);

        // 1. Limpiar items antiguos (se borran por orphanRemoval)
        existing.getItems().clear();

        // 2. Reconstruir la orden usando la existente como base
        Order rebuilt = buildOrderFromDto(dto, existing);

        // 3. Copiar los campos calculados a 'existing'
        existing.setUserEmail(rebuilt.getUserEmail());
        existing.setEstado(rebuilt.getEstado());
        existing.setCoupon(rebuilt.getCoupon());
        existing.setFinalPrice(rebuilt.getFinalPrice());
        existing.setDiscountApplied(rebuilt.getDiscountApplied());
        existing.setOrderDate(rebuilt.getOrderDate());

        // 4. Asignar los nuevos items a la entidad existente
        rebuilt.getItems().forEach(item -> item.setOrder(existing));
        existing.getItems().addAll(rebuilt.getItems());

        // 5. Guardar la entidad gestionada
        orderRepository.save(existing);

        return ResponseEntity.ok(convertToDTO(existing));
    }

    public ResponseEntity<MessageResponse> updateDonationStatusById(Long id, DonationStatus status, Long changedById) {
        if (status == null) {
            throw new IllegalArgumentException("El estado de la orden no puede ser nulo.");
        }
        Order order = getOrderById(id);
        DonationStatus estadoAnterior = order.getEstado();
        order.setEstado(status);
        orderRepository.save(order);

        trackingHistoryRepository.save(TrackingHistory.builder()
                .order(order)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(status)
                .changedById(changedById)
                .fechaCambio(LocalDateTime.now())
                .build());

        if (status == DonationStatus.EN_PREPARACION) {
            List<DonationItemEvent> items = order.getItems().stream()
                    .map(item -> new DonationItemEvent(item.getKitId(), item.getQuantity()))
                    .toList();
            donationEventPublisher.publishDonationConfirmed(
                    new DonationConfirmedEvent(order.getId(), order.getUserEmail(), items, LocalDateTime.now())
            );
        }

        return ResponseEntity.ok(new MessageResponse("Estado de la orden actualizado exitosamente: " + status));
    }

    public ResponseEntity<List<TrackingHistory>> getOrderHistory(Long id) {
        getOrderById(id); // valida existencia
        return ResponseEntity.ok(trackingHistoryRepository.findByOrder_IdOrderByFechaCambioAsc(id));
    }

    public ResponseEntity<MessageResponse> uploadTransferProof(Long id, byte[] proofBytes) {
        Order order = getOrderById(id);
        DonationStatus estadoAnterior = order.getEstado();
        order.setTransferProof(proofBytes);
        order.setTransferProofUploadedAt(LocalDateTime.now());
        order.setEstado(DonationStatus.INGRESADA);
        orderRepository.save(order);

        trackingHistoryRepository.save(TrackingHistory.builder()
                .order(order)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(DonationStatus.INGRESADA)
                .fechaCambio(LocalDateTime.now())
                .comentario("Comprobante de transferencia adjuntado")
                .build());

        donationEventPublisher.publishTransferSubmitted(
                new TransferSubmittedEvent(order.getId(), order.getUserEmail(), LocalDateTime.now())
        );

        return ResponseEntity.ok(new MessageResponse("Comprobante de transferencia recibido. Estado: INGRESADA"));
    }

    public ResponseEntity<MessageResponse> uploadDeliveryProof(Long id, byte[] photo, byte[] document) {
        Order order = getOrderById(id);
        DonationStatus estadoAnterior = order.getEstado();
        if (photo != null) order.setDeliveryPhoto(photo);
        if (document != null) order.setDeliveryDocument(document);
        order.setEstado(DonationStatus.PENDIENTE_CONFIRMACION);
        orderRepository.save(order);

        trackingHistoryRepository.save(TrackingHistory.builder()
                .order(order)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(DonationStatus.PENDIENTE_CONFIRMACION)
                .fechaCambio(LocalDateTime.now())
                .comentario("Foto y documento de entrega subidos")
                .build());

        return ResponseEntity.ok(new MessageResponse("Evidencia de entrega recibida. Estado: PENDIENTE_CONFIRMACION"));
    }

    public ResponseEntity<MessageResponse> confirmDelivery(Long id, Long confirmedById) {
        Order order = getOrderById(id);
        DonationStatus estadoAnterior = order.getEstado();
        order.setEstado(DonationStatus.ENTREGADA);
        order.setDeliveryConfirmedAt(LocalDateTime.now());
        order.setDeliveryConfirmedBy(confirmedById);
        orderRepository.save(order);

        trackingHistoryRepository.save(TrackingHistory.builder()
                .order(order)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(DonationStatus.ENTREGADA)
                .changedById(confirmedById)
                .fechaCambio(LocalDateTime.now())
                .comentario("Entrega confirmada")
                .build());

        return ResponseEntity.ok(new MessageResponse("Entrega confirmada. Estado: ENTREGADA"));
    }

    public ResponseEntity<MessageResponse> deleteOrder(Long id) {
        Order order = getOrderById(id);
        orderRepository.delete(order);
        return ResponseEntity.ok(new MessageResponse("Orden eliminada correctamente."));
    }

    public ResponseEntity<List<OrderResponse>> getByDonorEmail(String email) {
        return ResponseEntity.ok(orderRepository.findByUserEmail(email)
                .stream().map(this::convertToDTO).collect(Collectors.toList()));
    }

    public ResponseEntity<List<OrderResponse>> getByBeneficiary(Long beneficiaryId) {
        return ResponseEntity.ok(orderRepository.findByBeneficiaryId(beneficiaryId)
                .stream().map(this::convertToDTO).collect(Collectors.toList()));
    }

    public ResponseEntity<List<OrderResponse>> getByZone(Long zoneId) {
        return ResponseEntity.ok(orderRepository.findByZonaCatastrofeId(zoneId)
                .stream().map(this::convertToDTO).collect(Collectors.toList()));
    }

    public ResponseEntity<DashboardResponse> getDashboard() {
        List<Order> all = orderRepository.findAll();
        long totalDonations = all.size();
        long totalItems = all.stream().mapToLong(o -> o.getItems().size()).sum();
        Map<String, Long> byStatus = Arrays.stream(DonationStatus.values())
                .collect(Collectors.toMap(DonationStatus::name,
                        s -> all.stream().filter(o -> o.getEstado() == s).count()));
        Map<Long, Long> byZone = all.stream()
                .filter(o -> o.getZonaCatastrofeId() != null)
                .collect(Collectors.groupingBy(Order::getZonaCatastrofeId, Collectors.counting()));
        return ResponseEntity.ok(DashboardResponse.builder()
                .totalDonations(totalDonations).totalItems(totalItems)
                .donationsByStatus(byStatus).donationsByZone(byZone).build());
    }

    // =========================
    // Helpers de conversión
    // =========================
    private OrderResponse convertToDTO(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userEmail(order.getUserEmail())
                .estado(order.getEstado())
                .coupon(order.getCoupon() != null ? convertCouponToResponse(order.getCoupon()) : null)
                .discountApplied(order.getDiscountApplied())
                .finalPrice(order.getFinalPrice())
                .orderDate(order.getOrderDate())
                .items(order.getItems())
                .build();
    }

    private CouponResponse convertCouponToResponse(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .discountAmount(coupon.getDiscountAmount())
                .active(coupon.isActive())
                .build();
    }

    // =========================
    // Lógica de construcción de Order
    // =========================
    private Order buildOrderFromDto(OrderDto dto, Order currentOrder) {
        UserResponseDto user = userValidatorService.getUserByEmail(dto.getUserEmail());

        List<OrderItemRequestDto> itemRequests = dto.getItems();
        if (itemRequests == null || itemRequests.isEmpty()) {
            throw new IllegalArgumentException("La orden debe contener al menos un producto.");
        }

        List<OrderItem> items = itemRequests.stream()
                .map(this::buildOrderItem)
                .collect(Collectors.toList());

        int subtotal = items.stream()
                .mapToInt(OrderItem::getSubtotal)
                .sum();

        Coupon coupon = null;
        int desiredDiscount = ZERO;
        boolean newCouponApplied = false;

        if (StringUtils.hasText(dto.getCouponCode())) {
            coupon = couponService.getCouponByCode(dto.getCouponCode().trim());
            if (!coupon.isActive()) {
                throw new IllegalArgumentException("El cupón proporcionado no está disponible.");
            }
            desiredDiscount = (coupon.getDiscountAmount() != null)
                    ? coupon.getDiscountAmount()
                    : ZERO;
            newCouponApplied = true;
        } else if (currentOrder != null && currentOrder.getCoupon() != null) {
            coupon = currentOrder.getCoupon();
            desiredDiscount = (coupon.getDiscountAmount() != null)
                    ? coupon.getDiscountAmount()
                    : ZERO;
        }

        int discountApplied = Math.min(desiredDiscount, subtotal);
        int finalPrice = subtotal - discountApplied;

        Order order = Order.builder()
                .userEmail(user.getEmail())
                .estado(currentOrder != null ? currentOrder.getEstado() : DonationStatus.DRAFT)
                .coupon(coupon)
                .finalPrice(finalPrice)
                .discountApplied(discountApplied)
                .orderDate(currentOrder != null ? currentOrder.getOrderDate() : LocalDateTime.now())
                .campaignId(dto.getCampaignId())
                .items(items)
                .build();

        if (newCouponApplied && coupon != null) {
            couponService.updateCouponStatusByCode(coupon.getCode(), false);
        }

        items.forEach(item -> item.setOrder(order));
        return order;
    }

    // =========================
    // Construcción de OrderItem
    // =========================
    private OrderItem buildOrderItem(OrderItemRequestDto itemDto) {
        KitResponseDto kit = fetchKit(itemDto.getKitId());
        Integer kitPrice = kit.getPrecioEstimado();
        if (kitPrice == null) {
            throw new IllegalArgumentException("El kit " + kit.getId() + " no tiene precio definido.");
        }

        int quantity = itemDto.getQuantity();
        int subtotal = kitPrice * quantity;

        return OrderItem.builder()
                .kitId(kit.getId())
                .kitNameSnapshot(kit.getNombre())
                .unitPrice(kitPrice)
                .quantity(quantity)
                .subtotal(subtotal)
                .build();
    }

    // =========================
    // Cliente a catalog ms — kits
    // =========================
    private KitResponseDto fetchKit(Long kitId) {
        try {
            KitResponseDto kit = kitClient.getKitById(kitId);
            if (kit == null) {
                throw new ResourceNotFoundException("Kit no encontrado: " + kitId);
            }
            return kit;
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Kit no encontrado: " + kitId);
        } catch (FeignException e) {
            throw new IllegalArgumentException("Error al obtener el kit: " + e.getMessage());
        }
    }

}
