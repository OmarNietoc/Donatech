package com.donatech.order.service;

import com.donatech.order.client.CampaignClient;
import com.donatech.order.client.CompanyClient;
import com.donatech.order.client.KitClient;
import com.donatech.order.dto.CompanyDetailsDto;
import com.donatech.order.controller.response.CouponResponse;
import com.donatech.order.controller.response.DashboardResponse;
import com.donatech.order.controller.response.MessageResponse;
import com.donatech.order.controller.response.OrderResponse;
import com.donatech.order.controller.response.UserResponseDto;
import com.donatech.order.dto.AddItemToOrderRequest;
import com.donatech.order.dto.DonationDto;
import com.donatech.order.dto.KitResponseDto;
import com.donatech.order.dto.OrderDto;
import com.donatech.order.dto.OrderItemRequestDto;
import com.donatech.order.controller.response.DonationResponse;
import com.donatech.order.event.BeneficiaryShipmentEvent;
import com.donatech.order.event.BeneficiaryThankYouEvent;
import com.donatech.order.event.DeliveryConfirmRequestEvent;
import com.donatech.order.event.DeliverySubmittedEvent;
import com.donatech.order.event.DonationCancelledEvent;
import com.donatech.order.event.OrderShippedEvent;
import com.donatech.order.event.DonationEventPublisher;
import com.donatech.order.event.DonationItemEvent;
import com.donatech.order.event.OrderDeliveredEvent;
import com.donatech.order.event.TransferSubmittedEvent;
import com.donatech.order.exception.ResourceNotFoundException;
import com.donatech.order.model.Coupon;
import com.donatech.order.model.Donation;
import com.donatech.order.model.Order;
import com.donatech.order.model.OrderItem;
import com.donatech.order.model.DonationStatus;
import com.donatech.order.model.PaymentStatus;
import com.donatech.order.model.TrackingHistory;
import com.donatech.order.repository.DonationRepository;
import com.donatech.order.repository.OrderRepository;
import com.donatech.order.repository.TrackingHistoryRepository;
import feign.FeignException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final int ZERO = 0;

    // URL pública del frontend para construir el link de soporte en correos de entrega.
    // Reutiliza FRONTEND_URL (misma var del CORS del gateway); cae a localhost en dev.
    @org.springframework.beans.factory.annotation.Value("${app.frontend-url:${FRONTEND_URL:http://localhost:5173}}")
    private String frontendUrl;

    private final OrderRepository orderRepository;
    private final DonationRepository donationRepository;
    private final TrackingHistoryRepository trackingHistoryRepository;
    private final CouponService couponService;
    private final UserValidatorService userValidatorService;
    private final KitClient kitClient;
    private final CampaignClient campaignClient;
    private final DonationEventPublisher donationEventPublisher;
    private final ImageStorageService imageStorageService;
    private final CompanyClient companyClient;
    private final CertificateService certificateService;

    // Estados en los que el pago ya fue validado → se puede emitir el certificado.
    private static final Set<DonationStatus> CERTIFICATE_STATES = Set.of(
            DonationStatus.EN_PREPARACION, DonationStatus.ASIGNADA_ENVIO, DonationStatus.EN_CAMINO,
            DonationStatus.PENDIENTE_CONFIRMACION, DonationStatus.ENTREGADA);

    // Resuelve el beneficiario (user id) de la campaña asociada. Null-safe vía fallback Feign.
    private Long resolveBeneficiaryId(Long campaignId) {
        var campaign = fetchCampaign(campaignId);
        return campaign != null ? campaign.getBeneficiaryId() : null;
    }

    /**
     * Obtiene la campaña vía Feign con reintento: la primera llamada tras arrancar
     * puede caer al fallback (circuit breaker/cold start) devolviendo null.
     */
    private com.donatech.order.dto.CampaignSummaryDto fetchCampaign(Long campaignId) {
        if (campaignId == null) return null;
        for (int intento = 0; intento < 3; intento++) {
            var campaign = campaignClient.getCampaignById(campaignId);
            if (campaign != null) return campaign;
        }
        return null;
    }

    // Transiciones de estado permitidas. Los estados terminales no aparecen como clave.
    private static final Map<DonationStatus, Set<DonationStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(Map.of(
            DonationStatus.DRAFT, Set.of(DonationStatus.INGRESADA, DonationStatus.CANCELADA),
            DonationStatus.INGRESADA, Set.of(DonationStatus.EN_VALIDACION_TRANSFERENCIA, DonationStatus.CANCELADA),
            DonationStatus.EN_VALIDACION_TRANSFERENCIA, Set.of(DonationStatus.EN_PREPARACION, DonationStatus.RECHAZADA, DonationStatus.CANCELADA),
            // Flujo logístico secuencial: sin saltos. Preparación → asignar transportista
            // → en camino → evidencia de entrega. Cada paso exige el anterior.
            DonationStatus.EN_PREPARACION, Set.of(DonationStatus.ASIGNADA_ENVIO, DonationStatus.CANCELADA),
            DonationStatus.ASIGNADA_ENVIO, Set.of(DonationStatus.EN_CAMINO, DonationStatus.CANCELADA),
            DonationStatus.EN_CAMINO, Set.of(DonationStatus.PENDIENTE_CONFIRMACION, DonationStatus.CANCELADA),
            DonationStatus.PENDIENTE_CONFIRMACION, Set.of(DonationStatus.ENTREGADA)
    ));

    // Estados donde el stock ya fue descontado: cancelar exige restaurarlo en catalog
    private static final Set<DonationStatus> STOCK_DEDUCTED_STATES =
            Set.of(DonationStatus.EN_PREPARACION, DonationStatus.ASIGNADA_ENVIO);

    private void validateTransition(DonationStatus from, DonationStatus to) {
        Set<DonationStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new IllegalArgumentException(
                    "Transición de estado no permitida: " + from + " → " + to);
        }
    }

    private Authentication currentAuth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean isStaff(Authentication auth) {
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_VOLUNTARIO"));
    }

    private void requireOwnerOrStaff(String ownerEmail) {
        Authentication auth = currentAuth();
        if (auth == null || isStaff(auth)) return;
        if (ownerEmail == null || !ownerEmail.equalsIgnoreCase(auth.getName())) {
            throw new AccessDeniedException("No tienes permiso para acceder a esta orden.");
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<OrderResponse> getOrderDtoById(Long id) {
        Order order = getOrderById(id);
        requireOrderViewer(order);
        OrderResponse dto = convertToDTO(order);
        enrichDetail(dto, order);
        return ResponseEntity.ok(dto);
    }

    // Enriquece SOLO el detalle (no listados) con título de campaña y nombre del beneficiario,
    // resolviendo vía Feign de forma tolerante a fallos para no romper la respuesta.
    private void enrichDetail(OrderResponse dto, Order order) {
        if (order.getCampaignId() != null) {
            try {
                var campaign = campaignClient.getCampaignById(order.getCampaignId());
                if (campaign != null) dto.setCampaignTitulo(campaign.getTitulo());
            } catch (RuntimeException ignored) { /* fallback null */ }
        }
        var beneficiary = userValidatorService.getUserByIdSafe(order.getBeneficiaryId());
        if (beneficiary != null) dto.setBeneficiaryName(beneficiary.getName());

        // Donante: nombre de la persona o razón social si es organización (no el correo).
        if (!StringUtils.hasText(dto.getDonorName())) {
            dto.setDonorName(resolveDonorDisplayName(order.getUserEmail()));
        }
    }

    // Resuelve el nombre visible del donante: razón social (ORGANIZACION) o nombre del usuario; cae al email.
    private String resolveDonorDisplayName(String email) {
        if (email == null) return null;
        try {
            UserResponseDto user = userValidatorService.getUserByEmail(email);
            if (user == null) return email;
            boolean isOrg = user.getRole() != null && "ROLE_ORGANIZACION".equals(user.getRole().getName());
            if (isOrg) {
                try {
                    var company = companyClient.getCompany(user.getId());
                    if (company != null && StringUtils.hasText(company.getRazonSocial())) {
                        return company.getRazonSocial();
                    }
                } catch (RuntimeException ignored) { /* fallback al nombre del usuario */ }
            }
            return StringUtils.hasText(user.getName()) ? user.getName() : email;
        } catch (RuntimeException e) {
            return email;
        }
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada: " + id));
    }

    @Transactional(readOnly = true)
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
        requireOwnerOrStaff(userEmail);
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
        requireOwnerOrStaff(request.getUserEmail());
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
                    .beneficiaryId(resolveBeneficiaryId(request.getCampaignId()))
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

        trackingHistoryRepository.save(TrackingHistory.builder()
                .order(order)
                .estadoAnterior(null)
                .estadoNuevo(order.getEstado())
                .fechaCambio(LocalDateTime.now())
                .comentario("Orden de donación creada")
                .build());

        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(order));
    }

    // =========================
    // Crear donación (1 pago) con una orden por campaña
    // =========================
    @Transactional
    public ResponseEntity<DonationResponse> createDonation(@Valid DonationDto dto) {
        Donation donation = persistDonation(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDonationResponse(donation));
    }

    // Crea la Donation + sus órdenes hijas (una por grupo/campaña) y guarda historial. No adjunta comprobante.
    private Donation persistDonation(DonationDto dto) {
        if (dto.getGroups() == null || dto.getGroups().isEmpty()) {
            throw new IllegalArgumentException("La donación debe contener al menos una campaña.");
        }
        Donation donation = Donation.builder()
                .userEmail(dto.getUserEmail())
                .couponCode(dto.getCouponCode())
                .estadoPago(PaymentStatus.INGRESADA)
                .fechaCreacion(LocalDateTime.now())
                .build();

        int total = 0;
        int discount = 0;
        int idx = 0;
        for (DonationDto.Group g : dto.getGroups()) {
            // Cupón solo en la primera orden (evita multiplicar el descuento entre campañas).
            OrderDto od = new OrderDto(dto.getUserEmail(), g.getCampaignId(), g.getItems(),
                    idx == 0 ? dto.getCouponCode() : null);
            Order order = buildOrderFromDto(od, null);
            order.setDonation(donation);
            donation.getOrders().add(order);
            total += order.getFinalPrice();
            discount += order.getDiscountApplied();
            if (donation.getDonorName() == null && order.getDonorName() != null) {
                donation.setDonorName(order.getDonorName());
            }
            idx++;
        }
        donation.setTotal(total);
        donation.setDiscountApplied(discount);
        donationRepository.save(donation); // cascade ALL guarda las órdenes hijas

        for (Order o : donation.getOrders()) {
            trackingHistoryRepository.save(TrackingHistory.builder()
                    .order(o)
                    .estadoAnterior(null)
                    .estadoNuevo(o.getEstado())
                    .fechaCambio(LocalDateTime.now())
                    .comentario("Orden de donación creada")
                    .build());
        }
        return donation;
    }

    // Crear donación + comprobante en una sola operación atómica (checkout directo).
    @Transactional
    public ResponseEntity<DonationResponse> submitDonation(@Valid DonationDto dto, MultipartFile file, String uploaderEmail) throws java.io.IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Debes adjuntar el comprobante de transferencia.");
        }
        Donation donation = persistDonation(dto);
        attachProofAndSubmit(donation, file, uploaderEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDonationResponse(donation));
    }

    // Adjuntar comprobante a una donación existente → EN_VALIDACION_TRANSFERENCIA + transfer.submitted.
    @Transactional
    public ResponseEntity<MessageResponse> uploadDonationProof(Long donationId, MultipartFile file, String uploaderEmail) throws java.io.IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Debes adjuntar el comprobante de transferencia.");
        }
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donación no encontrada: " + donationId));
        requireOwnerOrStaff(donation.getUserEmail());
        if (donation.getEstadoPago() != PaymentStatus.INGRESADA
                && donation.getEstadoPago() != PaymentStatus.EN_VALIDACION_TRANSFERENCIA) {
            throw new IllegalArgumentException("La donación no admite comprobante en su estado actual: " + donation.getEstadoPago());
        }
        attachProofAndSubmit(donation, file, uploaderEmail);
        return ResponseEntity.ok(new MessageResponse("Comprobante recibido. Donación EN_VALIDACION_TRANSFERENCIA"));
    }

    private void attachProofAndSubmit(Donation donation, MultipartFile file, String uploaderEmail) throws java.io.IOException {
        String proofUrl = imageStorageService.store("transfer-proofs",
                uploaderEmail != null ? uploaderEmail : donation.getUserEmail(), file);
        donation.setTransferProofUrl(proofUrl);
        donation.setTransferProofUploadedAt(LocalDateTime.now());
        donation.setEstadoPago(PaymentStatus.EN_VALIDACION_TRANSFERENCIA);
        donationRepository.save(donation);
        donationEventPublisher.publishTransferSubmitted(
                new TransferSubmittedEvent(donation.getId(), donation.getUserEmail(), LocalDateTime.now()));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> getDonationProof(Long donationId) throws java.io.IOException {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donación no encontrada: " + donationId));
        requireOwnerOrStaff(donation.getUserEmail());
        if (donation.getTransferProofUrl() == null) return ResponseEntity.notFound().build();
        byte[] bytes = imageStorageService.load(donation.getTransferProofUrl());
        String ct = imageStorageService.detectContentType(donation.getTransferProofUrl());
        return ResponseEntity.ok().header("Content-Type", ct).body(bytes);
    }

    // Cancelar la donación: cancela sus órdenes hijas no terminales (restaura stock si ya estaba descontado).
    @Transactional
    public ResponseEntity<MessageResponse> cancelDonation(Long donationId, String motivo, Long changedById) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donación no encontrada: " + donationId));
        requireOwnerOrStaff(donation.getUserEmail());

        for (Order order : donation.getOrders()) {
            DonationStatus prev = order.getEstado();
            if (ORDER_TERMINAL.contains(prev)) continue;
            try {
                validateTransition(prev, DonationStatus.CANCELADA);
            } catch (RuntimeException e) {
                continue; // estado no cancelable, se omite
            }
            order.setEstado(DonationStatus.CANCELADA);
            if (StringUtils.hasText(motivo)) order.setRejectionReason(motivo);
            orderRepository.save(order);
            trackingHistoryRepository.save(TrackingHistory.builder()
                    .order(order)
                    .estadoAnterior(prev)
                    .estadoNuevo(DonationStatus.CANCELADA)
                    .changedById(changedById)
                    .fechaCambio(LocalDateTime.now())
                    .comentario(StringUtils.hasText(motivo) ? "Cancelada: " + motivo : "Donación cancelada")
                    .build());
            if (STOCK_DEDUCTED_STATES.contains(prev)) {
                List<DonationItemEvent> items = order.getItems().stream()
                        .map(item -> new DonationItemEvent(item.getKitId(), item.getQuantity()))
                        .toList();
                donationEventPublisher.publishDonationCancelled(
                        new DonationCancelledEvent(order.getId(), order.getUserEmail(), order.getCampaignId(), items, LocalDateTime.now()));
            }
        }
        if (donation.getEstadoPago() != PaymentStatus.APROBADA) {
            donation.setEstadoPago(PaymentStatus.CANCELADA);
        }
        if (StringUtils.hasText(motivo)) donation.setRejectionReason(motivo);
        donationRepository.save(donation);
        return ResponseEntity.ok(new MessageResponse("Donación cancelada."));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<DonationResponse>> getDonationsForDonor(String email) {
        requireOwnerOrStaff(email);
        List<Donation> list = donationRepository.findByUserEmailOrderByFechaCreacionDesc(email);
        return ResponseEntity.ok(list.stream().map(this::convertToDonationResponse).collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<DonationResponse> getDonationDtoById(Long id) {
        Donation d = donationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donación no encontrada: " + id));
        return ResponseEntity.ok(convertToDonationResponse(d, true));
    }

    private static final Set<DonationStatus> ORDER_TERMINAL = Set.of(
            DonationStatus.ENTREGADA, DonationStatus.CANCELADA, DonationStatus.RECHAZADA);

    // Donación activa = pago no terminal, o (aprobada y) con ≥1 orden no terminada.
    private boolean isDonationActiva(Donation d) {
        if (d.getEstadoPago() == PaymentStatus.RECHAZADA || d.getEstadoPago() == PaymentStatus.CANCELADA) return false;
        if (d.getEstadoPago() != PaymentStatus.APROBADA) return true;
        return d.getOrders().stream().anyMatch(o -> !ORDER_TERMINAL.contains(o.getEstado()));
    }

    private DonationResponse convertToDonationResponse(Donation d) {
        return convertToDonationResponse(d, false);
    }

    private DonationResponse convertToDonationResponse(Donation d, boolean enrich) {
        List<OrderResponse> orders = d.getOrders().stream().map(o -> {
            OrderResponse dto = convertToDTO(o);
            if (enrich) enrichDetail(dto, o);
            return dto;
        }).collect(Collectors.toList());
        return DonationResponse.builder()
                .id(d.getId())
                .userEmail(d.getUserEmail())
                .donorName(d.getDonorName())
                .estadoPago(d.getEstadoPago())
                .total(d.getTotal())
                .discountApplied(d.getDiscountApplied())
                .couponCode(d.getCouponCode())
                .transferProofUrl(d.getTransferProofUrl())
                .transferProofUploadedAt(d.getTransferProofUploadedAt())
                .rejectionReason(d.getRejectionReason())
                .fechaCreacion(d.getFechaCreacion())
                .activa(isDonationActiva(d))
                .orders(orders)
                .build();
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
        validateTransition(estadoAnterior, status);
        // donation.confirmed (descuento de stock) se publica únicamente desde
        // TransferValidatedConsumer para evitar descuentos duplicados.
        order.setEstado(status);
        orderRepository.save(order);

        trackingHistoryRepository.save(TrackingHistory.builder()
                .order(order)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(status)
                .changedById(changedById)
                .fechaCambio(LocalDateTime.now())
                .build());

        return ResponseEntity.ok(new MessageResponse("Estado de la orden actualizado exitosamente: " + status));
    }

    public ResponseEntity<MessageResponse> cancelOrder(Long id, String motivo, Long changedById) {
        Order order = getOrderById(id);
        requireOwnerOrStaff(order.getUserEmail());

        DonationStatus estadoAnterior = order.getEstado();
        validateTransition(estadoAnterior, DonationStatus.CANCELADA);

        order.setEstado(DonationStatus.CANCELADA);
        if (StringUtils.hasText(motivo)) {
            order.setRejectionReason(motivo);
        }
        orderRepository.save(order);

        trackingHistoryRepository.save(TrackingHistory.builder()
                .order(order)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(DonationStatus.CANCELADA)
                .changedById(changedById)
                .fechaCambio(LocalDateTime.now())
                .comentario(StringUtils.hasText(motivo) ? "Cancelada: " + motivo : "Donación cancelada")
                .build());

        if (STOCK_DEDUCTED_STATES.contains(estadoAnterior)) {
            List<DonationItemEvent> items = order.getItems().stream()
                    .map(item -> new DonationItemEvent(item.getKitId(), item.getQuantity()))
                    .toList();
            donationEventPublisher.publishDonationCancelled(
                    new DonationCancelledEvent(order.getId(), order.getUserEmail(), order.getCampaignId(), items, LocalDateTime.now())
            );
        }

        return ResponseEntity.ok(new MessageResponse("Donación cancelada. Estado: CANCELADA"));
    }

    public ResponseEntity<List<TrackingHistory>> getOrderHistory(Long id) {
        Order order = getOrderById(id); // valida existencia
        requireOrderViewer(order);
        return ResponseEntity.ok(trackingHistoryRepository.findByOrder_IdOrderByFechaCambioAsc(id));
    }

    @Transactional
    public ResponseEntity<MessageResponse> uploadTransferProof(Long id, MultipartFile file, String uploaderEmail) throws java.io.IOException {
        Order order = getOrderById(id);
        requireOwnerOrStaff(order.getUserEmail());
        DonationStatus estadoAnterior = order.getEstado();
        validateTransition(estadoAnterior, DonationStatus.EN_VALIDACION_TRANSFERENCIA);
        String proofUrl = imageStorageService.store("transfer-proofs", uploaderEmail, file);
        order.setTransferProofUrl(proofUrl);
        order.setTransferProofUploadedAt(LocalDateTime.now());
        order.setEstado(DonationStatus.EN_VALIDACION_TRANSFERENCIA);
        orderRepository.save(order);

        trackingHistoryRepository.save(TrackingHistory.builder()
                .order(order)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(DonationStatus.EN_VALIDACION_TRANSFERENCIA)
                .fechaCambio(LocalDateTime.now())
                .comentario("Comprobante de transferencia adjuntado")
                .build());

        Long donationId = order.getDonation() != null ? order.getDonation().getId() : order.getId();
        donationEventPublisher.publishTransferSubmitted(
                new TransferSubmittedEvent(donationId, order.getUserEmail(), LocalDateTime.now())
        );

        return ResponseEntity.ok(new MessageResponse("Comprobante de transferencia recibido. Estado: EN_VALIDACION_TRANSFERENCIA"));
    }

    @Transactional
    public ResponseEntity<MessageResponse> uploadDeliveryProof(Long id, MultipartFile photo, MultipartFile document, String uploaderEmail, Long callerId, boolean isAdmin) throws java.io.IOException {
        Order order = getOrderById(id);
        assertDeliveryOwner(order, callerId, isAdmin);
        DonationStatus estadoAnterior = order.getEstado();
        validateTransition(estadoAnterior, DonationStatus.PENDIENTE_CONFIRMACION);
        if (photo != null && !photo.isEmpty()) {
            order.setDeliveryPhotoUrl(imageStorageService.store("delivery-photos", uploaderEmail, photo));
        }
        if (document != null && !document.isEmpty()) {
            order.setDeliveryDocumentUrl(imageStorageService.store("delivery-docs", uploaderEmail, document));
        }
        order.setEstado(DonationStatus.PENDIENTE_CONFIRMACION);
        orderRepository.save(order);

        trackingHistoryRepository.save(TrackingHistory.builder()
                .order(order)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(DonationStatus.PENDIENTE_CONFIRMACION)
                .fechaCambio(LocalDateTime.now())
                .comentario("Foto y documento de entrega subidos")
                .build());

        donationEventPublisher.publishDeliverySubmitted(
                new DeliverySubmittedEvent(order.getId(), order.getUserEmail(), LocalDateTime.now())
        );

        // Correo al beneficiario con botón "Confirmar entrega" (o reportar a soporte).
        publishConfirmRequestToBeneficiary(order);

        return ResponseEntity.ok(new MessageResponse("Evidencia de entrega recibida. Estado: PENDIENTE_CONFIRMACION"));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> getTransferProof(Long id) throws java.io.IOException {
        Order order = getOrderById(id);
        requireOrderViewer(order);
        if (order.getTransferProofUrl() == null) return ResponseEntity.notFound().build();
        byte[] bytes = imageStorageService.load(order.getTransferProofUrl());
        String ct = imageStorageService.detectContentType(order.getTransferProofUrl());
        return ResponseEntity.ok().header("Content-Type", ct).body(bytes);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> getDeliveryPhoto(Long id) throws java.io.IOException {
        Order order = getOrderById(id);
        requireOrderViewer(order);
        if (order.getDeliveryPhotoUrl() == null) return ResponseEntity.notFound().build();
        byte[] bytes = imageStorageService.load(order.getDeliveryPhotoUrl());
        String ct = imageStorageService.detectContentType(order.getDeliveryPhotoUrl());
        return ResponseEntity.ok().header("Content-Type", ct).body(bytes);
    }

    // Certificado de donación (PDF) — solo donante ORGANIZACION y con pago validado.
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> getDonationCertificate(Long id) {
        Order order = getOrderById(id);
        requireOrderViewer(order);
        if (!CERTIFICATE_STATES.contains(order.getEstado())) {
            throw new IllegalArgumentException("El certificado de donación se emite una vez validado el pago.");
        }
        // El endpoint interno by-email no expone el rol; se identifica a la empresa por la
        // existencia de sus datos de empresa (company_details). Si no hay → no es empresa.
        UserResponseDto donor = userValidatorService.getUserByEmail(order.getUserEmail());
        Long userId = donor != null ? donor.getId() : null;
        CompanyDetailsDto company = userId != null ? companyClient.getCompany(userId) : null;
        if (company == null) {
            throw new IllegalArgumentException("El certificado de donación es solo para empresas.");
        }
        var campaign = fetchCampaign(order.getCampaignId());
        String titulo = campaign != null ? campaign.getTitulo() : null;
        byte[] pdf = certificateService.generate(order, company, titulo);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=certificado-donacion-" + id + ".pdf")
                .body(pdf);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> getDeliveryDocument(Long id) throws java.io.IOException {
        Order order = getOrderById(id);
        requireOrderViewer(order);
        if (order.getDeliveryDocumentUrl() == null) return ResponseEntity.notFound().build();
        byte[] bytes = imageStorageService.load(order.getDeliveryDocumentUrl());
        String ct = imageStorageService.detectContentType(order.getDeliveryDocumentUrl());
        return ResponseEntity.ok().header("Content-Type", ct).body(bytes);
    }

    public ResponseEntity<MessageResponse> confirmDelivery(Long id, Long confirmedById) {
        Order order = getOrderById(id);
        // La entrega la confirma el staff (ADMIN/VOLUNTARIO) o el beneficiario dueño de la campaña.
        requireCampaignBeneficiaryOrStaff(order.getCampaignId());
        DonationStatus estadoAnterior = order.getEstado();
        validateTransition(estadoAnterior, DonationStatus.ENTREGADA);
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

        List<DonationItemEvent> deliveredItems = order.getItems().stream()
                .map(item -> new DonationItemEvent(item.getKitId(), item.getQuantity()))
                .toList();
        donationEventPublisher.publishOrderDelivered(
                new OrderDeliveredEvent(order.getId(), order.getUserEmail(), order.getCampaignId(), deliveredItems, LocalDateTime.now())
        );

        return ResponseEntity.ok(new MessageResponse("Entrega confirmada. Estado: ENTREGADA"));
    }

    // =========================
    // Agradecimiento del beneficiario al donante
    // =========================
    @Transactional
    public ResponseEntity<MessageResponse> sendThankYou(Long id, String message, MultipartFile[] images, String callerEmail) throws java.io.IOException {
        Order order = getOrderById(id);
        requireCampaignBeneficiaryOrStaff(order.getCampaignId());
        if (order.getEstado() != DonationStatus.ENTREGADA) {
            throw new IllegalArgumentException("Solo puedes agradecer donaciones que ya fueron entregadas.");
        }
        if (!StringUtils.hasText(message) || message.length() > 600) {
            throw new IllegalArgumentException("El mensaje es obligatorio y no puede superar los 600 caracteres.");
        }

        List<String> paths = new java.util.ArrayList<>();
        if (images != null) {
            for (MultipartFile img : images) {
                if (img == null || img.isEmpty()) continue;
                String ct = img.getContentType();
                if (ct == null || !ct.startsWith("image/")) {
                    throw new IllegalArgumentException("Solo se permiten imágenes como adjuntos.");
                }
                paths.add(imageStorageService.store("thank-you",
                        callerEmail != null ? callerEmail : order.getUserEmail(), img));
            }
        }

        order.setThankYouMessage(message);
        order.setThankYouSentAt(LocalDateTime.now());
        order.getThankYouImageUrls().clear();
        order.getThankYouImageUrls().addAll(paths);
        orderRepository.save(order);

        String beneficiaryName = safeUserName(callerEmail);
        donationEventPublisher.publishBeneficiaryThankYou(new BeneficiaryThankYouEvent(
                order.getId(), order.getUserEmail(), order.getDonorName(), beneficiaryName, message, paths.size()
        ));

        return ResponseEntity.ok(new MessageResponse("Mensaje de agradecimiento enviado al donante."));
    }

    // Descarga de imagen de agradecimiento por índice — usada por notification ms (Feign).
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> getThankYouImage(Long id, int index) throws java.io.IOException {
        Order order = getOrderById(id);
        List<String> urls = order.getThankYouImageUrls();
        if (urls == null || index < 0 || index >= urls.size()) return ResponseEntity.notFound().build();
        byte[] bytes = imageStorageService.load(urls.get(index));
        String ct = imageStorageService.detectContentType(urls.get(index));
        return ResponseEntity.ok().header("Content-Type", ct).body(bytes);
    }

    private String safeUserName(String email) {
        if (email == null) return "Beneficiario";
        try {
            UserResponseDto user = userValidatorService.getUserByEmail(email);
            return (user != null && StringUtils.hasText(user.getName())) ? user.getName() : email;
        } catch (RuntimeException e) {
            return email;
        }
    }

    @Transactional
    public ResponseEntity<MessageResponse> assignCourier(Long id, String nombre, String contacto, Long changedById) {
        Order order = getOrderById(id);
        DonationStatus estadoAnterior = order.getEstado();
        validateTransition(estadoAnterior, DonationStatus.ASIGNADA_ENVIO);
        order.setTransportistaNombre(nombre);
        order.setTransportistaContacto(contacto);
        order.setCourierAssignedAt(LocalDateTime.now());
        order.setEstado(DonationStatus.ASIGNADA_ENVIO);
        orderRepository.save(order);

        trackingHistoryRepository.save(TrackingHistory.builder()
                .order(order)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(DonationStatus.ASIGNADA_ENVIO)
                .changedById(changedById)
                .fechaCambio(LocalDateTime.now())
                .comentario("Transportista asignado: " + nombre
                        + (StringUtils.hasText(contacto) ? " (" + contacto + ")" : ""))
                .build());

        return ResponseEntity.ok(new MessageResponse("Transportista asignado. Estado: ASIGNADA_ENVIO"));
    }

    // Asignación vía ruta (consumer route.assigned): EN_PREPARACION → ASIGNADA_ENVIO + colaborador.
    @Transactional
    public void assignFromRoute(Long id, String routeId, String routeName, Long collaboratorId, String collaboratorNombre, String collaboratorEmail) {
        Order order = getOrderById(id);
        DonationStatus estadoAnterior = order.getEstado();
        if (estadoAnterior == DonationStatus.ASIGNADA_ENVIO) {
            // ya asignada; solo actualizar colaborador/ruta
            order.setRouteId(routeId);
            order.setRouteName(routeName);
            order.setCollaboratorId(collaboratorId);
            order.setTransportistaNombre(collaboratorNombre);
            order.setTransportistaContacto(collaboratorEmail);
            orderRepository.save(order);
            return;
        }
        validateTransition(estadoAnterior, DonationStatus.ASIGNADA_ENVIO);
        order.setRouteId(routeId);
        order.setRouteName(routeName);
        order.setCollaboratorId(collaboratorId);
        order.setTransportistaNombre(collaboratorNombre);
        order.setTransportistaContacto(collaboratorEmail);
        order.setCourierAssignedAt(LocalDateTime.now());
        order.setEstado(DonationStatus.ASIGNADA_ENVIO);
        orderRepository.save(order);

        trackingHistoryRepository.save(TrackingHistory.builder()
                .order(order)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(DonationStatus.ASIGNADA_ENVIO)
                .changedById(collaboratorId)
                .fechaCambio(LocalDateTime.now())
                .comentario("Asignada al colaborador: " + collaboratorNombre)
                .build());
    }

    // El colaborador dueño solo puede operar SUS entregas (a menos que sea admin).
    private void assertDeliveryOwner(Order order, Long callerId, boolean isAdmin) {
        if (isAdmin) return;
        if (order.getCollaboratorId() != null && !order.getCollaboratorId().equals(callerId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "No puedes operar una entrega que no te fue asignada.");
        }
    }

    private static final Set<DonationStatus> DELIVERY_STATES = Set.of(
            DonationStatus.EN_PREPARACION, DonationStatus.ASIGNADA_ENVIO,
            DonationStatus.EN_CAMINO, DonationStatus.PENDIENTE_CONFIRMACION);

    // Entregas para la UI por ruta. ADMIN → todas; VOLUNTARIO → solo las suyas. Enriquecidas con beneficiario.
    @Transactional(readOnly = true)
    public ResponseEntity<List<OrderResponse>> getDeliveries(Long callerId, boolean isAdmin) {
        List<Order> orders = isAdmin
                ? orderRepository.findByEstadoIn(DELIVERY_STATES)
                : orderRepository.findByCollaboratorIdAndEstadoIn(callerId, DELIVERY_STATES);
        return ResponseEntity.ok(orders.stream().map(o -> {
            OrderResponse dto = convertToDTO(o);
            enrichDelivery(o, dto);
            return dto;
        }).collect(Collectors.toList()));
    }

    // Completa los datos del beneficiario (nombre/apellido/dirección/comuna/región) para diferenciar entregas.
    private void enrichDelivery(Order order, OrderResponse dto) {
        var c = userValidatorService.getContactByIdSafe(order.getBeneficiaryId());
        if (c == null) return;
        dto.setBeneficiaryName(c.getName());
        dto.setBeneficiaryApellido(c.getApellido());
        dto.setBeneficiaryDireccion(c.getDireccion());
        dto.setBeneficiaryComuna(c.getComuna());
        dto.setBeneficiaryRegion(c.getRegion());
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<OrderResponse>> getByCollaborator(Long collaboratorId) {
        List<Order> orders = orderRepository.findByCollaboratorIdAndEstadoIn(collaboratorId, Set.of(
                DonationStatus.ASIGNADA_ENVIO, DonationStatus.EN_CAMINO, DonationStatus.PENDIENTE_CONFIRMACION));
        return ResponseEntity.ok(orders.stream().map(this::convertToDTO).collect(Collectors.toList()));
    }

    @Transactional
    public ResponseEntity<MessageResponse> markInTransit(Long id, Long changedById, Long callerId, boolean isAdmin) {
        Order order = getOrderById(id);
        assertDeliveryOwner(order, callerId, isAdmin);
        DonationStatus estadoAnterior = order.getEstado();
        validateTransition(estadoAnterior, DonationStatus.EN_CAMINO);
        order.setEstado(DonationStatus.EN_CAMINO);
        orderRepository.save(order);

        trackingHistoryRepository.save(TrackingHistory.builder()
                .order(order)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(DonationStatus.EN_CAMINO)
                .changedById(changedById)
                .fechaCambio(LocalDateTime.now())
                .comentario("En camino hacia el beneficiario")
                .build());

        publishInTransitNotifications(order);

        return ResponseEntity.ok(new MessageResponse("Donación en camino. Estado: EN_CAMINO"));
    }

    // Al entrar EN_CAMINO: correo al donante (order.shipped) y al beneficiario (delivery.incoming).
    private void publishInTransitNotifications(Order order) {
        String transportista = order.getTransportistaNombre();
        String trackingInfo = (StringUtils.hasText(transportista) ? "Transportista: " + transportista + ". " : "")
                + "Entrega estimada en 1 día hábil.";
        donationEventPublisher.publishOrderShipped(
                new OrderShippedEvent(order.getId(), order.getUserEmail(), trackingInfo));

        var contact = userValidatorService.getContactByIdSafe(order.getBeneficiaryId());
        if (contact != null && StringUtils.hasText(contact.getEmail())) {
            String kitNames = order.getItems().stream()
                    .map(OrderItem::getKitNameSnapshot)
                    .filter(StringUtils::hasText)
                    .distinct()
                    .collect(Collectors.joining(", "));
            String supportLink = frontendUrl + "/support/delivery-issue?orderId=" + order.getId();
            donationEventPublisher.publishDeliveryIncoming(new BeneficiaryShipmentEvent(
                    order.getId(),
                    contact.getEmail(),
                    contact.getName(),
                    kitNames,
                    contact.getDireccion(),
                    contact.getPhone(),
                    supportLink,
                    "1 día hábil"));
        }
    }

    // Al subir evidencia (PENDIENTE_CONFIRMACION): correo al beneficiario para que confirme
    // la recepción desde el detalle de la orden, o reporte a soporte si no la recibió.
    private void publishConfirmRequestToBeneficiary(Order order) {
        var contact = userValidatorService.getContactByIdSafe(order.getBeneficiaryId());
        if (contact == null || !StringUtils.hasText(contact.getEmail())) return;
        String kitNames = order.getItems().stream()
                .map(OrderItem::getKitNameSnapshot)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.joining(", "));
        String confirmLink = frontendUrl + "/donation/" + order.getId();
        String supportLink = frontendUrl + "/support/delivery-issue?orderId=" + order.getId();
        donationEventPublisher.publishDeliveryConfirmRequest(new DeliveryConfirmRequestEvent(
                order.getId(), contact.getEmail(), contact.getName(), kitNames, confirmLink, supportLink));
    }

    public ResponseEntity<MessageResponse> deleteOrder(Long id) {
        Order order = getOrderById(id);
        orderRepository.delete(order);
        return ResponseEntity.ok(new MessageResponse("Orden eliminada correctamente."));
    }

    @Transactional
    public ResponseEntity<MessageResponse> deleteDonation(Long id) {
        Donation d = donationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donación no encontrada: " + id));
        // Borrar el historial de tracking de cada hija (FK) antes de eliminar las órdenes.
        for (Order o : d.getOrders()) {
            trackingHistoryRepository.deleteByOrder_Id(o.getId());
        }
        donationRepository.delete(d); // cascade ALL + orphanRemoval borra las órdenes hijas
        return ResponseEntity.ok(new MessageResponse("Donación eliminada correctamente."));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<OrderResponse>> getByDonorEmail(String email) {
        requireOwnerOrStaff(email);
        return ResponseEntity.ok(orderRepository.findByUserEmail(email)
                .stream().map(this::convertToDTO).collect(Collectors.toList()));
    }

    // Estados visibles para el beneficiario: desde que la donación entra en preparación.
    private static final Set<DonationStatus> BENEFICIARY_VISIBLE_STATES = Set.of(
            DonationStatus.EN_PREPARACION,
            DonationStatus.ASIGNADA_ENVIO,
            DonationStatus.EN_CAMINO,
            DonationStatus.PENDIENTE_CONFIRMACION,
            DonationStatus.ENTREGADA
    );

    @Transactional(readOnly = true)
    public ResponseEntity<List<OrderResponse>> getByCampaign(Long campaignId, boolean visibleOnly) {
        requireCampaignBeneficiaryOrStaff(campaignId);
        List<Order> orders = visibleOnly
                ? orderRepository.findByCampaignIdAndEstadoIn(campaignId, BENEFICIARY_VISIBLE_STATES)
                : orderRepository.findByCampaignId(campaignId);
        return ResponseEntity.ok(orders.stream().map(this::convertToDTO).collect(Collectors.toList()));
    }

    // El beneficiario dueño de la campaña (o el personal) puede ver/operar sus donaciones.
    private void requireCampaignBeneficiaryOrStaff(Long campaignId) {
        Authentication auth = currentAuth();
        if (auth == null || isStaff(auth)) return;
        Long campaignBeneficiaryId = resolveBeneficiaryId(campaignId);
        if (campaignBeneficiaryId == null) {
            throw new AccessDeniedException("No se pudo validar la propiedad de la campaña.");
        }
        UserResponseDto caller = userValidatorService.getUserByEmail(auth.getName());
        if (caller == null || !campaignBeneficiaryId.equals(caller.getId())) {
            throw new AccessDeniedException("No tienes permiso para ver las donaciones de esta campaña.");
        }
    }

    // Lectura de una orden: permitida a staff, al donante dueño y al beneficiario de la campaña
    // (las 3 partes ven el detalle/evidencias para transparencia).
    private void requireOrderViewer(Order order) {
        Authentication auth = currentAuth();
        if (auth == null || isStaff(auth)) return;
        if (order.getUserEmail() != null && order.getUserEmail().equalsIgnoreCase(auth.getName())) return;
        Long campaignBeneficiaryId = resolveBeneficiaryId(order.getCampaignId());
        if (campaignBeneficiaryId != null) {
            UserResponseDto caller = userValidatorService.getUserByEmail(auth.getName());
            if (caller != null && campaignBeneficiaryId.equals(caller.getId())) return;
        }
        throw new AccessDeniedException("No tienes permiso para ver esta donación.");
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<OrderResponse>> getByBeneficiary(Long beneficiaryId) {
        return ResponseEntity.ok(orderRepository.findByBeneficiaryId(beneficiaryId)
                .stream().map(this::convertToDTO).collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
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
                .donationId(order.getDonation() != null ? order.getDonation().getId() : null)
                .userEmail(order.getUserEmail())
                .donorName(order.getDonorName())
                .estado(order.getEstado())
                .coupon(order.getCoupon() != null ? convertCouponToResponse(order.getCoupon()) : null)
                .discountApplied(order.getDiscountApplied())
                .logisticsCost(order.getLogisticsCost())
                .finalPrice(order.getFinalPrice())
                .orderDate(order.getOrderDate())
                .campaignId(order.getCampaignId())
                .beneficiaryId(order.getBeneficiaryId())
                .routeId(order.getRouteId())
                .routeName(order.getRouteName())
                .zonaCatastrofeId(order.getZonaCatastrofeId())
                .rejectionReason(order.getRejectionReason())
                .deliveryConfirmedAt(order.getDeliveryConfirmedAt())
                .transferProofUrl(order.getTransferProofUrl())
                .transportistaNombre(order.getTransportistaNombre())
                .transportistaContacto(order.getTransportistaContacto())
                .courierAssignedAt(order.getCourierAssignedAt())
                .deliveryPhotoUrl(order.getDeliveryPhotoUrl())
                .deliveryDocumentUrl(order.getDeliveryDocumentUrl())
                .thankYouMessage(order.getThankYouMessage())
                .thankYouSentAt(order.getThankYouSentAt())
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

        // Campaña: consulta (con reintento) reutilizada para logística y beneficiaryId.
        var campaign = fetchCampaign(dto.getCampaignId());

        // Logística por unidad de kit (costo de la campaña × total de kits donados).
        int unidades = items.stream().mapToInt(OrderItem::getQuantity).sum();
        int logisticsCost = (campaign != null && campaign.getCostoLogistica() != null)
                ? campaign.getCostoLogistica() * unidades
                : 0;
        Long beneficiaryId = campaign != null ? campaign.getBeneficiaryId() : null;

        int discountApplied = Math.min(desiredDiscount, subtotal);
        int finalPrice = (subtotal + logisticsCost) - discountApplied;

        // Una orden creada completa (checkout) entra directo como INGRESADA;
        // el flujo de carrito (addItemToCart) mantiene DRAFT.
        Order order = Order.builder()
                .userEmail(user.getEmail())
                .estado(currentOrder != null ? currentOrder.getEstado() : DonationStatus.INGRESADA)
                .coupon(coupon)
                .finalPrice(finalPrice)
                .discountApplied(discountApplied)
                .logisticsCost(logisticsCost)
                .orderDate(currentOrder != null ? currentOrder.getOrderDate() : LocalDateTime.now())
                .campaignId(dto.getCampaignId())
                .beneficiaryId(beneficiaryId)
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
