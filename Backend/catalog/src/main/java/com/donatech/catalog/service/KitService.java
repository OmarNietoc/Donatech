package com.donatech.catalog.service;

import com.donatech.catalog.controller.response.MessageResponse;
import com.donatech.catalog.dto.KitDto;
import com.donatech.catalog.dto.KitItemDto;
import com.donatech.catalog.dto.response.KitItemResponseDto;
import com.donatech.catalog.dto.response.KitResponseDto;
import com.donatech.catalog.exception.ResourceNotFoundException;
import com.donatech.catalog.model.Kit;
import com.donatech.catalog.model.KitItem;
import com.donatech.catalog.model.Product;
import com.donatech.catalog.repository.KitRepository;
import com.donatech.catalog.repository.ProductRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KitService {

    private final KitRepository kitRepository;
    private final ProductRepository productRepository;
    private final ImageStorageService imageStorageService;

    public List<KitResponseDto> getAll() {
        return kitRepository.findAll().stream().map(this::toDto).toList();
    }

    public KitResponseDto getById(Long id) {
        return toDto(findById(id));
    }

    private Kit findById(Long id) {
        return kitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kit no encontrado: " + id));
    }

    private KitResponseDto toDto(Kit k) {
        List<KitItemResponseDto> items = k.getItems().stream().map(i -> KitItemResponseDto.builder()
                .id(i.getId())
                .productId(i.getProduct() != null ? i.getProduct().getId() : null)
                .productNombre(i.getProduct() != null ? i.getProduct().getNombre() : null)
                .productPrecio(i.getProduct() != null ? i.getProduct().getPrecio() : null)
                .cantidadRequerida(i.getCantidadRequerida())
                .build()).toList();

        return KitResponseDto.builder()
                .id(k.getId())
                .nombre(k.getNombre())
                .descripcion(k.getDescripcion())
                .activo(k.getActivo())
                .precioEstimado(k.getPrecioEstimado())
                .items(items)
                .hasImage(k.getImagenUrl() != null)
                .build();
    }

    public ResponseEntity<MessageResponse> create(@Valid KitDto dto) {
        Kit kit = Kit.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .activo(dto.getActivo() != null ? dto.getActivo() : 1)
                .precioEstimado(dto.getPrecioEstimado())
                .items(new ArrayList<>())
                .build();

        addItemsToKit(kit, dto.getItems());
        kitRepository.save(kit);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MessageResponse.builder().message("Kit creado exitosamente.").id(kit.getId()).build());
    }

    public ResponseEntity<MessageResponse> update(Long id, @Valid KitDto dto) {
        Kit kit = findById(id);
        kit.setNombre(dto.getNombre());
        kit.setDescripcion(dto.getDescripcion());
        if (dto.getActivo() != null) kit.setActivo(dto.getActivo());
        kit.setPrecioEstimado(dto.getPrecioEstimado());

        kit.getItems().clear();
        addItemsToKit(kit, dto.getItems());

        kitRepository.save(kit);
        return ResponseEntity.ok(new MessageResponse("Kit actualizado correctamente."));
    }

    @Transactional
    public ResponseEntity<MessageResponse> uploadImage(Long id, MultipartFile file) {
        Kit kit = findById(id);
        try {
            String path = imageStorageService.store("kits", String.valueOf(id), file);
            kit.setImagenUrl(path);
            kitRepository.save(kit);
            return ResponseEntity.ok(new MessageResponse("Imagen actualizada."));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al guardar imagen: " + e.getMessage()));
        }
    }

    public byte[] getImage(Long id) {
        Kit kit = findById(id);
        if (kit.getImagenUrl() == null) return null;
        try {
            return imageStorageService.load(kit.getImagenUrl());
        } catch (IOException e) {
            return null;
        }
    }

    public String getImageContentType(Long id) {
        return kitRepository.findById(id)
                .map(k -> k.getImagenUrl() != null ? imageStorageService.detectContentType(k.getImagenUrl()) : "image/jpeg")
                .orElse("image/jpeg");
    }

    public ResponseEntity<MessageResponse> delete(Long id) {
        Kit kit = findById(id);
        kitRepository.delete(kit);
        return ResponseEntity.ok(new MessageResponse("Kit eliminado correctamente."));
    }

    private void addItemsToKit(Kit kit, List<KitItemDto> itemDtos) {
        if (itemDtos == null) return;
        for (KitItemDto itemDto : itemDtos) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + itemDto.getProductId()));
            KitItem item = KitItem.builder()
                    .kit(kit)
                    .product(product)
                    .cantidadRequerida(itemDto.getCantidadRequerida())
                    .build();
            kit.getItems().add(item);
        }
    }
}
