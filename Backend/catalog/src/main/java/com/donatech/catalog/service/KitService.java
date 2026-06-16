package com.donatech.catalog.service;

import com.donatech.catalog.controller.response.MessageResponse;
import com.donatech.catalog.dto.KitDto;
import com.donatech.catalog.dto.KitItemDto;
import com.donatech.catalog.dto.response.KitItemResponseDto;
import com.donatech.catalog.dto.response.KitResponseDto;
import com.donatech.catalog.exception.ConflictException;
import com.donatech.catalog.exception.ResourceNotFoundException;
import com.donatech.catalog.model.CampaignKit;
import com.donatech.catalog.model.Kit;
import com.donatech.catalog.model.KitItem;
import com.donatech.catalog.model.KitTipo;
import com.donatech.catalog.model.Product;
import com.donatech.catalog.repository.CampaignKitRepository;
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
    private final CampaignKitRepository campaignKitRepository;
    private final ImageStorageService imageStorageService;

    /**
     * Precio del kit = suma de (precio producto × cantidad).
     * La logística NO se incluye aquí: es un costo de la campaña (varía por destino)
     * y se aplica por unidad de kit al crear la donación (ms order).
     */
    private int calcularPrecio(Kit kit) {
        return kit.getItems().stream()
                .mapToInt(i -> (i.getProduct() != null ? i.getProduct().getPrecio() : 0)
                        * i.getCantidadRequerida())
                .sum();
    }

    /**
     * Lista kits filtrando por tipo:
     * <ul>
     *   <li>{@code STANDARD} (o null): solo kits generales reutilizables (default).</li>
     *   <li>{@code USO_UNICO}: solo kits personalizados (con su campaña).</li>
     *   <li>{@code ALL}: todos.</li>
     * </ul>
     */
    public List<KitResponseDto> getAll(String tipo) {
        String filtro = tipo == null ? "STANDARD" : tipo.trim().toUpperCase();
        return kitRepository.findAll().stream()
                .filter(k -> switch (filtro) {
                    case "USO_UNICO" -> k.getTipo() == KitTipo.USO_UNICO;
                    case "ALL" -> true;
                    default -> k.getTipo() != KitTipo.USO_UNICO; // STANDARD / null
                })
                .map(this::toDto)
                .toList();
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
                .productHasImage(i.getProduct() != null && i.getProduct().getImagenUrl() != null)
                .build()).toList();

        Long campaignId = null;
        String campaignTitulo = null;
        if (k.getTipo() == KitTipo.USO_UNICO) {
            // Personalizado: adjunta la campaña a la que está vinculado (si la hay).
            campaignId = campaignKitRepository.findByKitId(k.getId()).stream()
                    .findFirst()
                    .map(ck -> ck.getCampaign() != null ? ck.getCampaign().getId() : null)
                    .orElse(null);
            campaignTitulo = campaignKitRepository.findByKitId(k.getId()).stream()
                    .findFirst()
                    .map(ck -> ck.getCampaign() != null ? ck.getCampaign().getTitulo() : null)
                    .orElse(null);
        }

        return KitResponseDto.builder()
                .id(k.getId())
                .nombre(k.getNombre())
                .descripcion(k.getDescripcion())
                .activo(k.getActivo())
                .tipo(k.getTipo() != null ? k.getTipo() : KitTipo.STANDARD)
                .precioEstimado(k.getPrecioEstimado())
                .items(items)
                .hasImage(k.getImagenUrl() != null)
                .campaignId(campaignId)
                .campaignTitulo(campaignTitulo)
                .build();
    }

    public ResponseEntity<MessageResponse> create(@Valid KitDto dto) {
        Kit kit = Kit.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .activo(dto.getActivo() != null ? dto.getActivo() : 1)
                .tipo(dto.getTipo() != null ? dto.getTipo() : KitTipo.STANDARD)
                .items(new ArrayList<>())
                .build();

        addItemsToKit(kit, dto.getItems());
        kit.setPrecioEstimado(calcularPrecio(kit)); // siempre automático
        kitRepository.save(kit);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MessageResponse.builder().message("Kit creado exitosamente.").id(kit.getId()).build());
    }

    public ResponseEntity<MessageResponse> createPersonalized(@Valid KitDto dto) {
        // Kit de uso único atado a una campaña; el tipo se fuerza por seguridad.
        dto.setTipo(KitTipo.USO_UNICO);
        return create(dto);
    }

    public ResponseEntity<MessageResponse> update(Long id, @Valid KitDto dto) {
        Kit kit = findById(id);
        kit.setNombre(dto.getNombre());
        kit.setDescripcion(dto.getDescripcion());
        if (dto.getActivo() != null) kit.setActivo(dto.getActivo());
        if (dto.getTipo() != null) kit.setTipo(dto.getTipo());

        kit.getItems().clear();
        addItemsToKit(kit, dto.getItems());
        kit.setPrecioEstimado(calcularPrecio(kit)); // siempre automático

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
        if (campaignKitRepository.existsByKitId(id)) {
            throw new ConflictException(
                    "No se puede eliminar el kit porque está en uso en una o más campañas. "
                    + "Quítalo de las campañas antes de eliminarlo.");
        }
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
