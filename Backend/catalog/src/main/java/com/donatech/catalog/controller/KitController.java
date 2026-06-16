package com.donatech.catalog.controller;

import com.donatech.catalog.controller.response.MessageResponse;
import com.donatech.catalog.dto.KitDto;
import com.donatech.catalog.dto.response.KitResponseDto;
import com.donatech.catalog.service.KitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/kits")
@RequiredArgsConstructor
@Tag(name = "Kits", description = "Gestión de kits de insumos para donaciones")
public class KitController {

    private final KitService kitService;

    @Operation(summary = "Listar kits (tipo: STANDARD por defecto, USO_UNICO, ALL)")
    @GetMapping
    public ResponseEntity<List<KitResponseDto>> getAll(
            @RequestParam(value = "tipo", required = false) String tipo) {
        return ResponseEntity.ok(kitService.getAll(tipo));
    }

    @Operation(summary = "Obtener kit por ID")
    @GetMapping("/{id}")
    public ResponseEntity<KitResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(kitService.getById(id));
    }

    @Operation(summary = "Imagen de kit")
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        byte[] bytes = kitService.getImage(id);
        if (bytes == null || bytes.length == 0) return ResponseEntity.notFound().build();
        String ct = kitService.getImageContentType(id);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(ct)).body(bytes);
    }

    @Operation(summary = "Subir imagen de kit")
    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return kitService.uploadImage(id, file);
    }

    @Operation(summary = "Crear kit")
    @PostMapping
    public ResponseEntity<MessageResponse> create(@Valid @RequestBody KitDto dto) {
        return kitService.create(dto);
    }

    @Operation(summary = "Crear kit personalizado (USO_UNICO) — para afectados vía asistente IA")
    @PostMapping("/personalized")
    public ResponseEntity<MessageResponse> createPersonalized(@Valid @RequestBody KitDto dto) {
        return kitService.createPersonalized(dto);
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
