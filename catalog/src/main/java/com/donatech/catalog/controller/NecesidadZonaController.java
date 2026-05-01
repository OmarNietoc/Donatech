package com.donatech.catalog.controller;

import com.donatech.catalog.controller.response.MessageResponse;
import com.donatech.catalog.dto.NecesidadZonaDto;
import com.donatech.catalog.model.NecesidadZona;
import com.donatech.catalog.service.NecesidadZonaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/necesidades")
@RequiredArgsConstructor
@Tag(name = "Necesidades por Zona", description = "Gestión de necesidades de insumos por zona de catástrofe")
public class NecesidadZonaController {

    private final NecesidadZonaService necesidadZonaService;

    @Operation(summary = "Listar todas las necesidades")
    @GetMapping
    public ResponseEntity<List<NecesidadZona>> getAll() {
        return ResponseEntity.ok(necesidadZonaService.getAll());
    }

    @Operation(summary = "Obtener necesidad por ID")
    @GetMapping("/{id}")
    public ResponseEntity<NecesidadZona> getById(@PathVariable Long id) {
        return ResponseEntity.ok(necesidadZonaService.getById(id));
    }

    @Operation(summary = "Necesidades por comuna")
    @GetMapping("/by-comuna/{comunaId}")
    public ResponseEntity<List<NecesidadZona>> getByComuna(@PathVariable Long comunaId) {
        return ResponseEntity.ok(necesidadZonaService.getByComuna(comunaId));
    }

    @Operation(summary = "Necesidades por producto")
    @GetMapping("/by-producto/{productoId}")
    public ResponseEntity<List<NecesidadZona>> getByProducto(@PathVariable String productoId) {
        return ResponseEntity.ok(necesidadZonaService.getByProducto(productoId));
    }

    @Operation(summary = "Registrar necesidad")
    @PostMapping
    public ResponseEntity<MessageResponse> create(@Valid @RequestBody NecesidadZonaDto dto) {
        return necesidadZonaService.create(dto);
    }

    @Operation(summary = "Actualizar necesidad")
    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> update(@PathVariable Long id, @Valid @RequestBody NecesidadZonaDto dto) {
        return necesidadZonaService.update(id, dto);
    }

    @Operation(summary = "Eliminar necesidad")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        return necesidadZonaService.delete(id);
    }
}
