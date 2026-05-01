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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
@Tag(name = "Donations", description = "Gestión de donaciones")
public class DonationController {

    private final OrderService orderService;

    @Operation(summary = "Listar todas las donaciones")
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

    @Operation(summary = "Actualizar estado de donación")
    @PatchMapping("/{id}/status")
    public ResponseEntity<MessageResponse> updateStatus(@PathVariable Long id,
                                                        @RequestParam DonationStatus status) {
        return orderService.updateDonationStatusById(id, status);
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
    @GetMapping("/by-zone/{zoneId}")
    public ResponseEntity<List<OrderResponse>> getByZone(@PathVariable Long zoneId) {
        return orderService.getByZone(zoneId);
    }

    @Operation(summary = "Eliminar donación")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        return orderService.deleteOrder(id);
    }
}
