package com.donatech.catalog.service;

import com.donatech.catalog.controller.response.MessageResponse;
import com.donatech.catalog.dto.KitDto;
import com.donatech.catalog.dto.KitItemDto;
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

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KitService {

    private final KitRepository kitRepository;
    private final ProductRepository productRepository;

    public List<Kit> getAll() {
        return kitRepository.findAll();
    }

    public Kit getById(Long id) {
        return kitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kit no encontrado: " + id));
    }

    public ResponseEntity<MessageResponse> create(@Valid KitDto dto) {
        Kit kit = Kit.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .imagen(dto.getImagen())
                .activo(dto.getActivo() != null ? dto.getActivo() : 1)
                .precioEstimado(dto.getPrecioEstimado())
                .items(new ArrayList<>())
                .build();

        addItemsToKit(kit, dto.getItems());
        kitRepository.save(kit);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Kit creado exitosamente."));
    }

    public ResponseEntity<MessageResponse> update(Long id, @Valid KitDto dto) {
        Kit kit = getById(id);
        kit.setNombre(dto.getNombre());
        kit.setDescripcion(dto.getDescripcion());
        kit.setImagen(dto.getImagen());
        if (dto.getActivo() != null) kit.setActivo(dto.getActivo());
        kit.setPrecioEstimado(dto.getPrecioEstimado());

        kit.getItems().clear();
        addItemsToKit(kit, dto.getItems());

        kitRepository.save(kit);
        return ResponseEntity.ok(new MessageResponse("Kit actualizado correctamente."));
    }

    public ResponseEntity<MessageResponse> delete(Long id) {
        Kit kit = getById(id);
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
