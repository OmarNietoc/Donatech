package com.donatech.order.controller;

import com.donatech.order.controller.response.MessageResponse;
import com.donatech.order.model.TransferConfig;
import com.donatech.order.repository.TransferConfigRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config/transfer")
@RequiredArgsConstructor
@Tag(name = "Transfer Config", description = "Datos bancarios para transferencias")
public class TransferConfigController {

    private static final Long CONFIG_ID = 1L;

    private final TransferConfigRepository transferConfigRepository;

    @GetMapping
    @Operation(summary = "Obtener datos bancarios para transferencias")
    public ResponseEntity<?> get() {
        return transferConfigRepository.findById(CONFIG_ID)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(new MessageResponse("No hay configuración de transferencia disponible")));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar datos bancarios (solo ADMIN)")
    public ResponseEntity<?> save(@Valid @RequestBody TransferConfig body) {
        body.setId(CONFIG_ID);
        TransferConfig saved = transferConfigRepository.save(body);
        return ResponseEntity.ok(saved);
    }
}
