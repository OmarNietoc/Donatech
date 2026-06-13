package com.donatech.order.controller;

import com.donatech.order.controller.response.MessageResponse;
import com.donatech.order.controller.response.OrderResponse;
import com.donatech.order.dto.OrderDto;
import com.donatech.order.model.DonationStatus;
import com.donatech.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
@Tag(name = "Donations", description = "Gestión de donaciones")
public class DonationController {

    private final OrderService orderService;

    @Operation(summary = "Listar todas las donaciones — solo personal autorizado")
    @PreAuthorize("hasAnyRole('ADMIN','VOLUNTARIO')")
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAll() {
        return orderService.getAllOrders();
    }

    @Operation(summary = "Obtener donación por ID")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        return orderService.getOrderDtoById(id);
    }

    @Operation(summary = "Crear donación")
    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderDto dto) {
        return orderService.createOrder(dto);
    }

    @Operation(summary = "Crear donación y adjuntar comprobante en una sola operación atómica",
            description = "Crea la orden y adjunta el comprobante de transferencia dentro de la misma " +
                    "transacción: la orden queda EN_VALIDACION_TRANSFERENCIA. Evita órdenes huérfanas.")
    @PostMapping(value = "/submit", consumes = "multipart/form-data")
    public ResponseEntity<OrderResponse> submit(
            @Valid @RequestPart("order") OrderDto dto,
            @RequestPart("file") MultipartFile file,
            @RequestHeader(value = "X-User-Email", required = false) String uploaderEmail) throws IOException {
        return orderService.submitDonation(dto, file, uploaderEmail);
    }

    @Operation(summary = "Actualizar estado de donación")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<MessageResponse> updateStatus(@PathVariable Long id,
                                                        @RequestParam DonationStatus status,
                                                        @RequestParam(required = false) Long changedById) {
        return orderService.updateDonationStatusById(id, status, changedById);
    }

    @Operation(summary = "Donaciones por donante (email)")
    @GetMapping("/by-donor")
    public ResponseEntity<List<OrderResponse>> getByDonor(@RequestParam String email) {
        return orderService.getByDonorEmail(email);
    }

    @Operation(summary = "Donaciones por beneficiario")
    @GetMapping("/by-beneficiary/{beneficiaryId}")
    public ResponseEntity<List<OrderResponse>> getByBeneficiary(@PathVariable Long beneficiaryId) {
        return orderService.getByBeneficiary(beneficiaryId);
    }

    @Operation(summary = "Donaciones por zona de catástrofe")
    @PreAuthorize("hasAnyRole('ADMIN','VOLUNTARIO')")
    @GetMapping("/by-zone/{zoneId}")
    public ResponseEntity<List<OrderResponse>> getByZone(@PathVariable Long zoneId) {
        return orderService.getByZone(zoneId);
    }

    @Operation(summary = "Donaciones recibidas por una campaña",
            description = "Permite al beneficiario/organización ver las donaciones de su campaña. " +
                    "Con visibleOnly=true solo devuelve las que están EN_PREPARACION en adelante.")
    @PreAuthorize("hasAnyRole('ADMIN','VOLUNTARIO','BENEFICIARIO','ORGANIZACION')")
    @GetMapping("/by-campaign/{campaignId}")
    public ResponseEntity<List<OrderResponse>> getByCampaign(
            @PathVariable Long campaignId,
            @RequestParam(defaultValue = "false") boolean visibleOnly) {
        return orderService.getByCampaign(campaignId, visibleOnly);
    }

    @Operation(summary = "Cancelar donación",
            description = "El donante (dueño) o un administrador cancela la donación.")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<MessageResponse> cancel(@PathVariable Long id,
                                                  @RequestParam(required = false) String motivo,
                                                  @RequestParam(required = false) Long changedById) {
        return orderService.cancelOrder(id, motivo, changedById);
    }

    @Operation(summary = "Eliminar donación")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        return orderService.deleteOrder(id);
    }
}
