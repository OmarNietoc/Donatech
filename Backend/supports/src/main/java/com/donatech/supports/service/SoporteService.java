package com.donatech.supports.service;

import com.donatech.supports.controller.response.MessageResponse;
import com.donatech.supports.dto.ResponderDTO;
import com.donatech.supports.dto.SoporteRequestDTO;
import com.donatech.supports.event.CampaignResultEvent;
import com.donatech.supports.event.CampaignResultPublisher;
import com.donatech.supports.event.TransferResultEvent;
import com.donatech.supports.event.TransferResultPublisher;
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
    private final CampaignResultPublisher campaignResultPublisher;
    private final TransferResultPublisher transferResultPublisher;

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
                .titulo(dto.getTitulo())
                .usuarioId(dto.getUsuarioId())
                .prioridad(dto.getPrioridad())
                .tipo(dto.getTipo())
                .donationId(dto.getDonationId())
                .campaignId(dto.getCampaignId())
                .recipientEmail(dto.getRecipientEmail())
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

    public ResponseEntity<MessageResponse> validateCampaign(Long ticketId, boolean approved, String motivo, Integer logistica) {
        Soporte soporte = obtenerSoportePorId(ticketId);
        soporte.setEstado(approved ? EstadoSoporte.RESUELTO : EstadoSoporte.CERRADO);
        soporte.setRespuesta(motivo);
        soporte.setFechaResolucion(LocalDateTime.now());
        soporteRepository.save(soporte);

        campaignResultPublisher.publish(new CampaignResultEvent(
                soporte.getCampaignId(), approved, motivo, soporte.getRecipientEmail(),
                logistica != null ? logistica : 0));
        return ResponseEntity.ok(new MessageResponse("Campaña " + (approved ? "aprobada" : "rechazada") + " correctamente."));
    }

    public ResponseEntity<MessageResponse> validateTransfer(Long ticketId, boolean approved, String motivo) {
        Soporte soporte = obtenerSoportePorId(ticketId);
        soporte.setEstado(approved ? EstadoSoporte.RESUELTO : EstadoSoporte.CERRADO);
        soporte.setRespuesta(motivo);
        soporte.setFechaResolucion(LocalDateTime.now());
        soporteRepository.save(soporte);

        transferResultPublisher.publish(new TransferResultEvent(soporte.getDonationId(), approved, motivo, soporte.getRecipientEmail()));
        return ResponseEntity.ok(new MessageResponse("Transferencia " + (approved ? "aprobada" : "rechazada") + " correctamente."));
    }
}
