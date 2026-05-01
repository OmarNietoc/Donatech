package com.donatech.supports.service;

import com.donatech.supports.controller.response.MessageResponse;
import com.donatech.supports.dto.ResponderDTO;
import com.donatech.supports.dto.SoporteRequestDTO;
import com.donatech.supports.exception.ResourceNotFoundException;
import com.donatech.supports.model.EstadoSoporte;
import com.donatech.supports.model.Soporte;
import com.donatech.supports.model.TipoSoporte;
import com.donatech.supports.repository.SoporteRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SoporteService {

    private final SoporteRepository soporteRepository;

    public List<Soporte> obtenerTodas() {
        return soporteRepository.findAll();
    }

    public Soporte obtenerSoportePorId(Long id) {
        return soporteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket de soporte no encontrado: " + id));
    }

    public ResponseEntity<Soporte> crear(@Valid SoporteRequestDTO dto) {
        Soporte soporte = Soporte.builder()
                .descripcion(dto.getDescripcion())
                .usuarioId(dto.getUsuarioId())
                .prioridad(dto.getPrioridad())
                .tipo(dto.getTipo())
                .donationId(dto.getDonationId())
                .estado(EstadoSoporte.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(soporteRepository.save(soporte));
    }

    public ResponseEntity<MessageResponse> eliminar(Long id) {
        obtenerSoportePorId(id);
        soporteRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("Soporte eliminado correctamente."));
    }

    public ResponseEntity<Soporte> actualizarEstado(Long id, EstadoSoporte nuevoEstado) {
        Soporte soporte = obtenerSoportePorId(id);
        soporte.setEstado(nuevoEstado);
        if (nuevoEstado == EstadoSoporte.RESUELTO) {
            soporte.setFechaResolucion(LocalDateTime.now());
        }
        return ResponseEntity.ok(soporteRepository.save(soporte));
    }

    public ResponseEntity<Soporte> asignar(Long id, Long voluntarioId) {
        Soporte soporte = obtenerSoportePorId(id);
        soporte.setAsignadoA(voluntarioId);
        soporte.setEstado(EstadoSoporte.EN_PROGRESO);
        return ResponseEntity.ok(soporteRepository.save(soporte));
    }

    public ResponseEntity<Soporte> responder(Long id, @Valid ResponderDTO dto) {
        Soporte soporte = obtenerSoportePorId(id);
        soporte.setRespuesta(dto.getRespuesta());
        soporte.setEstado(EstadoSoporte.RESUELTO);
        soporte.setFechaResolucion(LocalDateTime.now());
        return ResponseEntity.ok(soporteRepository.save(soporte));
    }

    public List<Soporte> getByEstado(EstadoSoporte estado) {
        return soporteRepository.findByEstado(estado);
    }

    public List<Soporte> getByTipo(TipoSoporte tipo) {
        return soporteRepository.findByTipo(tipo);
    }

    public List<Soporte> getByUsuario(Long usuarioId) {
        return soporteRepository.findByUsuarioId(usuarioId);
    }

    public List<Soporte> getByVoluntario(Long voluntarioId) {
        return soporteRepository.findByAsignadoA(voluntarioId);
    }

    public List<Soporte> getByDonation(Long donationId) {
        return soporteRepository.findByDonationId(donationId);
    }
}
