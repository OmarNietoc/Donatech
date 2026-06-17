package com.donatech.order.controller;

import com.donatech.order.controller.response.DashboardResponse;
import com.donatech.order.controller.response.MessageResponse;
import com.donatech.order.controller.response.OrderResponse;
import com.donatech.order.dto.OrderDto;
import com.donatech.order.model.DonationStatus;
import com.donatech.order.model.TrackingHistory;
import com.donatech.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.donatech.order.dto.AddItemToOrderRequest;
import com.donatech.order.dto.AssignCourierRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Listar órdenes", description = "Obtiene todas las órdenes registradas. Solo personal autorizado.")
    @ApiResponse(responseCode = "200", description = "Órdenes listadas correctamente")
    @PreAuthorize("hasAnyRole('ADMIN','VOLUNTARIO')")
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return orderService.getAllOrders();
    }

    @Operation(summary = "Obtener orden por ID", description = "Devuelve los detalles de una orden específica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orden encontrada"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return orderService.getOrderDtoById(id);
    }

    @Operation(summary = "Crear nueva orden", description = "Registra una nueva orden con sus ítems.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Orden creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderDto dto) {
        return orderService.createOrder(dto);
    }

    @Operation(summary = "Actualizar una orden", description = "Reemplaza los ítems y el cupón de una orden existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orden actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable Long id, @Valid @RequestBody OrderDto dto) {
        return orderService.updateOrder(id, dto);
    }

    // Obtener carrito activo por usuario
    @Operation(
            summary = "Obtener carrito activo por usuario",
            description = "Devuelve la orden en estado PENDING para el email indicado, incluyendo todos sus ítems."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrito encontrado"),
            @ApiResponse(responseCode = "404", description = "El usuario no tiene un carrito activo")
    })
    @GetMapping("/cart")
    public ResponseEntity<OrderResponse> getActiveOrderByUserEmail(
            @RequestParam String userEmail
    ) {
        return orderService.getActiveOrderByUserEmail(userEmail);
    }


    // Agregar ítem al carrito sin reconstruir todo
    @Operation(
            summary = "Agregar producto al carrito",
            description = "Agrega un producto al carrito (orden en estado PENDING) del usuario. " +
                    "Si el usuario aún no tiene carrito, se crea uno nuevo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Carrito creado y producto agregado"),
            @ApiResponse(responseCode = "200", description = "Producto agregado o actualizado en el carrito existente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuario o producto no encontrado")
    })
    @PostMapping("/cart/items")
    public ResponseEntity<OrderResponse> addItemToCart(
            @Valid @RequestBody AddItemToOrderRequest request
    ) {
        return orderService.addItemToCart(request);
    }

    @Operation(summary = "Actualizar estado de la orden", description = "Cambia el estado de una orden específica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado inválido"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<MessageResponse> updateDonationStatus(
            @PathVariable Long id,
            @RequestParam DonationStatus status,
            @RequestParam(required = false) Long changedById) {
        return orderService.updateDonationStatusById(id, status, changedById);
    }

    @Operation(summary = "Historial de cambios de estado de una orden")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial obtenido correctamente"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    @GetMapping("/{id}/history")
    public ResponseEntity<List<TrackingHistory>> getOrderHistory(@PathVariable Long id) {
        return orderService.getOrderHistory(id);
    }

    @Operation(summary = "Eliminar una orden", description = "Elimina una orden por su ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orden eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteOrder(@PathVariable Long id) {
        return orderService.deleteOrder(id);
    }

    @Operation(summary = "Subir comprobante de transferencia",
            description = "Adjunta el comprobante bancario y cambia el estado a EN_VALIDACION_TRANSFERENCIA.")
    @ApiResponse(responseCode = "200", description = "Comprobante recibido")
    @PostMapping(value = "/{id}/transfer-proof", consumes = "multipart/form-data")
    public ResponseEntity<MessageResponse> uploadTransferProof(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-User-Email", defaultValue = "unknown") String uploaderEmail) throws IOException {
        return orderService.uploadTransferProof(id, file, uploaderEmail);
    }

    @Operation(summary = "Descargar comprobante de transferencia")
    @ApiResponse(responseCode = "200", description = "Archivo del comprobante")
    @GetMapping("/{id}/transfer-proof")
    public ResponseEntity<byte[]> getTransferProof(@PathVariable Long id) throws IOException {
        return orderService.getTransferProof(id);
    }

    @Operation(summary = "Asignar transportista",
            description = "Asigna nombre/contacto del transportista. Cambia estado EN_PREPARACION → ASIGNADA_ENVIO.")
    @ApiResponse(responseCode = "200", description = "Transportista asignado")
    @PreAuthorize("hasAnyRole('ADMIN','VOLUNTARIO')")
    @PatchMapping("/{id}/assign-courier")
    public ResponseEntity<MessageResponse> assignCourier(
            @PathVariable Long id,
            @Valid @RequestBody AssignCourierRequest body,
            @RequestParam(required = false) Long changedById) {
        return orderService.assignCourier(id, body.getTransportistaNombre(), body.getTransportistaContacto(), changedById);
    }

    @Operation(summary = "Marcar en camino",
            description = "El transportista entra en ruta. Cambia estado ASIGNADA_ENVIO → EN_CAMINO.")
    @ApiResponse(responseCode = "200", description = "En camino")
    @PreAuthorize("hasAnyRole('ADMIN','VOLUNTARIO')")
    @PatchMapping("/{id}/in-transit")
    public ResponseEntity<MessageResponse> markInTransit(
            @PathVariable Long id,
            @RequestParam(required = false) Long changedById,
            @RequestHeader(value = "X-User-Id", required = false) Long callerId,
            @RequestHeader(value = "X-User-Roles", defaultValue = "") String roles) {
        return orderService.markInTransit(id, changedById, callerId, roles.contains("ROLE_ADMIN"));
    }

    @Operation(summary = "Entregas asignadas a un colaborador")
    @PreAuthorize("hasAnyRole('ADMIN','VOLUNTARIO')")
    @GetMapping("/by-collaborator/{collaboratorId}")
    public ResponseEntity<List<OrderResponse>> getByCollaborator(@PathVariable Long collaboratorId) {
        return orderService.getByCollaborator(collaboratorId);
    }

    @Operation(summary = "Entregas por ruta (ADMIN = todas; VOLUNTARIO = solo las suyas)",
            description = "Órdenes en estados de entrega, enriquecidas con datos del beneficiario, para la UI por ruta.")
    @PreAuthorize("hasAnyRole('ADMIN','VOLUNTARIO')")
    @GetMapping("/deliveries")
    public ResponseEntity<List<OrderResponse>> getDeliveries(
            @RequestHeader(value = "X-User-Id", required = false) Long callerId,
            @RequestHeader(value = "X-User-Roles", defaultValue = "") String roles) {
        return orderService.getDeliveries(callerId, roles.contains("ROLE_ADMIN"));
    }

    @Operation(summary = "Subir evidencia de entrega",
            description = "Transportista sube foto y documento firmado. Cambia estado EN_CAMINO → PENDIENTE_CONFIRMACION.")
    @ApiResponse(responseCode = "200", description = "Evidencia recibida")
    @PreAuthorize("hasAnyRole('ADMIN','VOLUNTARIO')")
    @PostMapping(value = "/{id}/delivery-proof", consumes = "multipart/form-data")
    public ResponseEntity<MessageResponse> uploadDeliveryProof(
            @PathVariable Long id,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "document", required = false) MultipartFile document,
            @RequestHeader(value = "X-User-Email", defaultValue = "unknown") String uploaderEmail,
            @RequestHeader(value = "X-User-Id", required = false) Long callerId,
            @RequestHeader(value = "X-User-Roles", defaultValue = "") String roles) throws IOException {
        return orderService.uploadDeliveryProof(id, photo, document, uploaderEmail, callerId, roles.contains("ROLE_ADMIN"));
    }

    @Operation(summary = "Descargar foto de entrega")
    @GetMapping("/{id}/delivery-photo")
    public ResponseEntity<byte[]> getDeliveryPhoto(@PathVariable Long id) throws IOException {
        return orderService.getDeliveryPhoto(id);
    }

    @Operation(summary = "Descargar documento de entrega")
    @GetMapping("/{id}/delivery-document")
    public ResponseEntity<byte[]> getDeliveryDocument(@PathVariable Long id) throws IOException {
        return orderService.getDeliveryDocument(id);
    }

    @Operation(summary = "Descargar certificado de donación (PDF)",
            description = "Solo donante ORGANIZACION con pago validado. Llenado con los datos de la empresa.")
    @GetMapping("/{id}/donation-certificate")
    public ResponseEntity<byte[]> getDonationCertificate(@PathVariable Long id) {
        return orderService.getDonationCertificate(id);
    }

    @Operation(summary = "Confirmar entrega",
            description = "El admin o el beneficiario dueño confirma la recepción. Cambia estado a ENTREGADA.")
    @ApiResponse(responseCode = "200", description = "Entrega confirmada")
    @PreAuthorize("hasAnyRole('ADMIN','BENEFICIARIO','ORGANIZACION')")
    @PatchMapping("/{id}/confirm-delivery")
    public ResponseEntity<MessageResponse> confirmDelivery(
            @PathVariable Long id,
            @RequestParam Long confirmedById) {
        return orderService.confirmDelivery(id, confirmedById);
    }

    @Operation(summary = "Cancelar donación",
            description = "El donante (dueño) o un administrador cancela la orden. " +
                    "Si el stock ya fue descontado, se publica donation.cancelled para restaurarlo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Donación cancelada"),
            @ApiResponse(responseCode = "400", description = "El estado actual no permite cancelar"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<MessageResponse> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String motivo,
            @RequestParam(required = false) Long changedById) {
        return orderService.cancelOrder(id, motivo, changedById);
    }

    @Operation(summary = "Dashboard — resumen de órdenes por estado y zona")
    @GetMapping("/dashboard/summary")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return orderService.getDashboard();
    }

    @Operation(summary = "Agradecer al donante",
            description = "El beneficiario redacta un mensaje (máx 600) con imágenes para agradecer. " +
                    "Solo disponible cuando la donación está ENTREGADA.")
    @PreAuthorize("hasAnyRole('ADMIN','VOLUNTARIO','BENEFICIARIO','ORGANIZACION')")
    @PostMapping(value = "/{id}/thank-you", consumes = "multipart/form-data")
    public ResponseEntity<MessageResponse> sendThankYou(
            @PathVariable Long id,
            @RequestParam("message") String message,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @RequestHeader(value = "X-User-Email", required = false) String callerEmail) throws IOException {
        return orderService.sendThankYou(id, message, images, callerEmail);
    }

    @Operation(summary = "Descargar imagen de agradecimiento (uso interno notification ms)")
    @GetMapping("/{id}/thank-you-image/{index}")
    public ResponseEntity<byte[]> getThankYouImage(@PathVariable Long id, @PathVariable int index) throws IOException {
        return orderService.getThankYouImage(id, index);
    }
}
