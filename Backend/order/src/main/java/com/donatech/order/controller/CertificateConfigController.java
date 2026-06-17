package com.donatech.order.controller;

import com.donatech.order.controller.response.MessageResponse;
import com.donatech.order.model.CertificateConfig;
import com.donatech.order.repository.CertificateConfigRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config/certificate")
@RequiredArgsConstructor
@Tag(name = "Certificate Config", description = "Textos configurables del certificado de donación")
public class CertificateConfigController {

    private static final Long CONFIG_ID = 1L;

    private final CertificateConfigRepository repository;

    @Operation(summary = "Obtener textos del certificado")
    @GetMapping
    public ResponseEntity<?> get() {
        return repository.findById(CONFIG_ID)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(new MessageResponse("No hay configuración de certificado disponible")));
    }

    @Operation(summary = "Actualizar textos del certificado (solo ADMIN)")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CertificateConfig> save(@RequestBody CertificateConfig body) {
        body.setId(CONFIG_ID);
        return ResponseEntity.ok(repository.save(body));
    }
}
