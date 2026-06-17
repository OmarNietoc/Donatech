package com.donatech.order.controller;

import com.donatech.order.controller.response.DonationResponse;
import com.donatech.order.controller.response.MessageResponse;
import com.donatech.order.controller.response.OrderResponse;
import com.donatech.order.dto.DonationDto;
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

    @Operation(summary = "Obtener donación por ID (con sus órdenes hijas)")
    @GetMapping("/{id}")
    public ResponseEntity<DonationResponse> getById(@PathVariable Long id) {
        return orderService.getDonationDtoById(id);
    }

    @Operation(summary = "Crear donación", description = "Crea un pago que agrupa una orden por campaña.")
    @PostMapping
    public ResponseEntity<DonationResponse> create(@Valid @RequestBody DonationDto dto) {
        return orderService.createDonation(dto);
    }

    @Operation(summary = "Crear donación y adjuntar comprobante en una sola operación atómica",
            description = "Crea la orden y adjunta el comprobante de transferencia dentro de la misma " +
                    "transacción: la orden queda EN_VALIDACION_TRANSFERENCIA. Evita órdenes huérfanas.")
    @PostMapping(value = "/submit", consumes = "multipart/form-data")
    public ResponseEntity<DonationResponse> submit(
            @Valid @RequestPart("donation") DonationDto dto,
            @RequestPart("file") MultipartFile file,
            @RequestHeader(value = "X-User-Email", required = false) String uploaderEmail) throws IOException {
        return orderService.submitDonation(dto, file, uploaderEmail);
    }

    @Operation(summary = "Subir comprobante de transferencia de la donación",
            description = "Adjunta el comprobante (un solo pago) y pasa la donación a EN_VALIDACION_TRANSFERENCIA.")
    @PostMapping(value = "/{id}/transfer-proof", consumes = "multipart/form-data")
    public ResponseEntity<MessageResponse> uploadProof(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-User-Email", defaultValue = "unknown") String uploaderEmail) throws IOException {
        return orderService.uploadDonationProof(id, file, uploaderEmail);
    }

    @Operation(summary = "Descargar comprobante de transferencia de la donación")
    @GetMapping("/{id}/transfer-proof")
    public ResponseEntity<byte[]> getProof(@PathVariable Long id) throws IOException {
        return orderService.getDonationProof(id);
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
    public ResponseEntity<List<DonationResponse>> getByDonor(@RequestParam String email) {
        return orderService.getDonationsForDonor(email);
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
            description = "El donante (dueño) o un administrador cancela la donación y sus órdenes no terminales.")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<MessageResponse> cancel(@PathVariable Long id,
                                                  @RequestParam(required = false) String motivo,
                                                  @RequestParam(required = false) Long changedById) {
        return orderService.cancelDonation(id, motivo, changedById);
    }

    @Operation(summary = "Eliminar donación (y sus órdenes hijas)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        return orderService.deleteDonation(id);
    }
}
