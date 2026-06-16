package com.donatech.catalog.controller;

import com.donatech.catalog.controller.response.MessageResponse;
import com.donatech.catalog.dto.CampaignKitDto;
import com.donatech.catalog.dto.CampaignRequestDto;
import com.donatech.catalog.dto.UpdateCampaignKitDto;
import com.donatech.catalog.dto.response.CampaignImageDto;
import com.donatech.catalog.dto.response.CampaignResponseDto;
import com.donatech.catalog.service.CampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
@Tag(name = "Campañas", description = "Gestión de campañas de donación humanitaria")
public class CampaignController {

    private final CampaignService campaignService;

    @Operation(summary = "Listar todas las campañas")
    @GetMapping
    public ResponseEntity<List<CampaignResponseDto>> getAll() {
        return ResponseEntity.ok(campaignService.getAll());
    }

    @Operation(summary = "Listar campañas activas — visibles para donantes")
    @GetMapping("/active")
    public ResponseEntity<List<CampaignResponseDto>> getAllActive() {
        return ResponseEntity.ok(campaignService.getAllActive());
    }

    @Operation(summary = "Campañas por beneficiario")
    @GetMapping("/by-beneficiary/{beneficiaryId}")
    public ResponseEntity<List<CampaignResponseDto>> getByBeneficiary(@PathVariable Long beneficiaryId) {
        return ResponseEntity.ok(campaignService.getByBeneficiary(beneficiaryId));
    }

    @Operation(summary = "Obtener campaña por ID")
    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.getById(id));
    }

    @Operation(summary = "Crear campaña — queda en estado EN_VALIDACION")
    @PostMapping
    public ResponseEntity<MessageResponse> create(@Valid @RequestBody CampaignRequestDto dto) {
        return campaignService.create(dto);
    }

    @Operation(summary = "Agregar kit a campaña")
    @PostMapping("/{id}/kits")
    public ResponseEntity<MessageResponse> addKit(
            @PathVariable Long id,
            @Valid @RequestBody CampaignKitDto dto) {
        return campaignService.addKit(id, dto);
    }

    @Operation(summary = "Actualizar cantidad necesaria de un kit en la campaña")
    @PatchMapping("/{id}/kits/{kitId}")
    public ResponseEntity<MessageResponse> updateKitQuantity(
            @PathVariable Long id,
            @PathVariable Long kitId,
            @Valid @RequestBody UpdateCampaignKitDto dto) {
        return campaignService.updateKitQuantity(id, kitId, dto.getCantidadNecesaria());
    }

    @Operation(summary = "Remover kit de campaña")
    @DeleteMapping("/{id}/kits/{kitId}")
    public ResponseEntity<MessageResponse> removeKit(
            @PathVariable Long id,
            @PathVariable Long kitId) {
        return campaignService.removeKit(id, kitId);
    }

    @Operation(summary = "Actualizar costo de logística por kit (solo campaña ACTIVA)")
    @PatchMapping("/{id}/logistica")
    public ResponseEntity<MessageResponse> updateLogistica(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Integer> body) {
        return campaignService.updateLogistica(id, body.get("monto"));
    }

    @Operation(summary = "Finalizar campaña")
    @PatchMapping("/{id}/close")
    public ResponseEntity<MessageResponse> close(@PathVariable Long id) {
        return campaignService.close(id);
    }

    // ─── Imágenes de campaña ────────────────────────────────────────────────

    @Operation(summary = "Listar metadatos de imágenes de campaña")
    @GetMapping("/{id}/images")
    public ResponseEntity<List<CampaignImageDto>> getImages(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.getCampaignImages(id));
    }

    @Operation(summary = "Subir imagen a campaña (máx 3)")
    @PostMapping(value = "/{id}/images", consumes = "multipart/form-data")
    public ResponseEntity<CampaignImageDto> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-User-Email", defaultValue = "unknown") String uploaderEmail) throws IOException {
        return ResponseEntity.status(201).body(campaignService.uploadCampaignImage(id, file, uploaderEmail));
    }

    @Operation(summary = "Obtener imagen de campaña (bytes, público)")
    @GetMapping("/{id}/images/{imageId}")
    public ResponseEntity<byte[]> getImage(
            @PathVariable Long id,
            @PathVariable Long imageId) throws IOException {
        byte[] bytes = campaignService.getCampaignImageBytes(id, imageId);
        String contentType = campaignService.getCampaignImageContentType(id, imageId);
        return ResponseEntity.ok()
                .header("Content-Type", contentType)
                .body(bytes);
    }

    @Operation(summary = "Eliminar imagen de campaña")
    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<MessageResponse> deleteImage(
            @PathVariable Long id,
            @PathVariable Long imageId) {
        return campaignService.deleteCampaignImage(id, imageId);
    }
}
