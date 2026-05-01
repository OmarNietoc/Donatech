package com.donatech.catalog.controller;

import com.donatech.catalog.controller.response.MessageResponse;
import com.donatech.catalog.dto.KitDto;
import com.donatech.catalog.model.Kit;
import com.donatech.catalog.service.KitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kits")
@RequiredArgsConstructor
@Tag(name = "Kits", description = "Gestión de kits de insumos para donaciones")
public class KitController {

    private final KitService kitService;

    @Operation(summary = "Listar todos los kits")
    @GetMapping
    public ResponseEntity<List<Kit>> getAll() {
        return ResponseEntity.ok(kitService.getAll());
    }

    @Operation(summary = "Obtener kit por ID")
    @GetMapping("/{id}")
    public ResponseEntity<Kit> getById(@PathVariable Long id) {
        return ResponseEntity.ok(kitService.getById(id));
    }

    @Operation(summary = "Crear kit")
    @PostMapping
    public ResponseEntity<MessageResponse> create(@Valid @RequestBody KitDto dto) {
        return kitService.create(dto);
    }

    @Operation(summary = "Actualizar kit")
    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> update(@PathVariable Long id, @Valid @RequestBody KitDto dto) {
        return kitService.update(id, dto);
    }

    @Operation(summary = "Eliminar kit")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        return kitService.delete(id);
    }
}
