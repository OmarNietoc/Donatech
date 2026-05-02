package com.donatech.catalog.service;

import com.donatech.catalog.controller.response.MessageResponse;
import com.donatech.catalog.dto.NecesidadZonaDto;
import com.donatech.catalog.exception.ResourceNotFoundException;
import com.donatech.catalog.model.NecesidadZona;
import com.donatech.catalog.model.Product;
import com.donatech.catalog.repository.NecesidadZonaRepository;
import com.donatech.catalog.repository.ProductRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NecesidadZonaService {

    private final NecesidadZonaRepository necesidadZonaRepository;
    private final ProductRepository productRepository;

    public List<NecesidadZona> getAll() {
        return necesidadZonaRepository.findAll();
    }

    public List<NecesidadZona> getByComuna(Long comunaId) {
        return necesidadZonaRepository.findByComunaId(comunaId);
    }

    public List<NecesidadZona> getByProducto(String productoId) {
        return necesidadZonaRepository.findByProducto_Id(productoId);
    }

    public NecesidadZona getById(Long id) {
        return necesidadZonaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Necesidad no encontrada: " + id));
    }

    public ResponseEntity<MessageResponse> create(@Valid NecesidadZonaDto dto) {
        Product producto = productRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + dto.getProductoId()));
        NecesidadZona necesidad = NecesidadZona.builder()
                .producto(producto)
                .comunaId(dto.getComunaId())
                .cantidadNecesaria(dto.getCantidadNecesaria())
                .cantidadCubierta(dto.getCantidadCubierta() != null ? dto.getCantidadCubierta() : 0)
                .build();
        necesidadZonaRepository.save(necesidad);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Necesidad registrada exitosamente."));
    }

    public ResponseEntity<MessageResponse> update(Long id, @Valid NecesidadZonaDto dto) {
        NecesidadZona necesidad = getById(id);
        Product producto = productRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + dto.getProductoId()));
        necesidad.setProducto(producto);
        necesidad.setComunaId(dto.getComunaId());
        necesidad.setCantidadNecesaria(dto.getCantidadNecesaria());
        if (dto.getCantidadCubierta() != null) necesidad.setCantidadCubierta(dto.getCantidadCubierta());
        necesidadZonaRepository.save(necesidad);
        return ResponseEntity.ok(new MessageResponse("Necesidad actualizada correctamente."));
    }

    public ResponseEntity<MessageResponse> delete(Long id) {
        NecesidadZona necesidad = getById(id);
        necesidadZonaRepository.delete(necesidad);
        return ResponseEntity.ok(new MessageResponse("Necesidad eliminada correctamente."));
    }
}
