package com.donatech.users.controller;

import com.donatech.users.dto.BeneficiaryDto;
import com.donatech.users.dto.VerifyBeneficiaryDto;
import com.donatech.users.model.Beneficiary;
import com.donatech.users.model.EstadoVerificacion;
import com.donatech.users.service.BeneficiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiaries")
@RequiredArgsConstructor
@Tag(name = "Beneficiaries", description = "Gestión de beneficiarios")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    @GetMapping
    @Operation(summary = "Listar todos los beneficiarios")
    public ResponseEntity<List<Beneficiary>> getAll() {
        return ResponseEntity.ok(beneficiaryService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener beneficiario por ID")
    public ResponseEntity<Beneficiary> getById(@PathVariable Long id) {
        return ResponseEntity.ok(beneficiaryService.getById(id));
    }

    @GetMapping("/by-user/{userId}")
    @Operation(summary = "Obtener beneficiario por User ID")
    public ResponseEntity<Beneficiary> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(beneficiaryService.getByUserId(userId));
    }

    @GetMapping("/by-estado")
    @Operation(summary = "Filtrar beneficiarios por estado de verificación")
    public ResponseEntity<List<Beneficiary>> getByEstado(@RequestParam EstadoVerificacion estado) {
        return ResponseEntity.ok(beneficiaryService.getByEstado(estado));
    }

    @PostMapping
    @Operation(summary = "Registrar beneficiario")
    public ResponseEntity<Beneficiary> create(@Valid @RequestBody BeneficiaryDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(beneficiaryService.create(dto));
    }

    @PatchMapping("/{id}/verify")
    @Operation(summary = "Verificar o rechazar beneficiario")
    public ResponseEntity<Beneficiary> verify(@PathVariable Long id,
                                               @Valid @RequestBody VerifyBeneficiaryDto dto) {
        return ResponseEntity.ok(beneficiaryService.verify(id, dto.getEstado(), dto.getVerificadorId(), dto.getMotivoRechazo()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar beneficiario")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        beneficiaryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
