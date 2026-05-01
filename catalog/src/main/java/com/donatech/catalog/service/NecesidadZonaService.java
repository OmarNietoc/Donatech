package com.donatech.catalog.service;

import com.donatech.catalog.controller.response.MessageResponse;
import com.donatech.catalog.dto.NecesidadZonaDto;
import com.donatech.catalog.exception.ResourceNotFoundException;
import com.donatech.catalog.model.NecesidadZona;
import com.donatech.catalog.repository.NecesidadZonaRepository;
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

    public List<NecesidadZona> getAll() {
        return necesidadZonaRepository.findAll();
    }

    public List<NecesidadZona> getByComuna(Long comunaId) {
        return necesidadZonaRepository.findByComunaId(comunaId);
    }

    public List<NecesidadZona> getByProducto(String productoId) {
        return necesidadZonaRepository.findByProductoId(productoId);
    }

    public NecesidadZona getById(Long id) {
        return necesidadZonaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Necesidad no encontrada: " + id));
    }

    public ResponseEntity<MessageResponse> create(@Valid NecesidadZonaDto dto) {
        NecesidadZona necesidad = NecesidadZona.builder()
                .productoId(dto.getProductoId())
                .comunaId(dto.getComunaId())
                .cantidadNecesaria(dto.getCantidadNecesaria())
                .cantidadCubierta(dto.getCantidadCubierta() != null ? dto.getCantidadCubierta() : 0)
                .build();
        necesidadZonaRepository.save(necesidad);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Necesidad registrada exitosamente."));
    }

    public ResponseEntity<MessageResponse> update(Long id, @Valid NecesidadZonaDto dto) {
        NecesidadZona necesidad = getById(id);
        necesidad.setProductoId(dto.getProductoId());
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
