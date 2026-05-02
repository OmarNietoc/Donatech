package com.donatech.supports.service;

import com.donatech.supports.controller.response.MessageResponse;
import com.donatech.supports.dto.SoporteRequestDTO;
import com.donatech.supports.event.CampaignResultPublisher;
import com.donatech.supports.event.TransferResultPublisher;
import com.donatech.supports.exception.ResourceNotFoundException;
import com.donatech.supports.model.EstadoSoporte;
import com.donatech.supports.model.Soporte;
import com.donatech.supports.model.TipoSoporte;
import com.donatech.supports.repository.SoporteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SoporteServiceTest {

    @Mock SoporteRepository soporteRepository;
    @Mock CampaignResultPublisher campaignResultPublisher;
    @Mock TransferResultPublisher transferResultPublisher;

    @InjectMocks SoporteService soporteService;

    @Test
    void obtenerTodas_returnsList() {
        when(soporteRepository.findAll()).thenReturn(List.of(new Soporte()));

        List<Soporte> result = soporteService.obtenerTodas();

        assertThat(result).hasSize(1);
    }

    @Test
    void obtenerSoportePorId_exists_returnsSoporte() {
        Soporte soporte = Soporte.builder().id(1L).descripcion("Test").build();
        when(soporteRepository.findById(1L)).thenReturn(Optional.of(soporte));

        Soporte result = soporteService.obtenerSoportePorId(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void obtenerSoportePorId_notFound_throwsException() {
        when(soporteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> soporteService.obtenerSoportePorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void crear_validDto_savesSoporteWithPendienteEstado() {
        SoporteRequestDTO dto = new SoporteRequestDTO();
        dto.setDescripcion("Problema con campaña");
        dto.setTitulo("Título ticket");
        dto.setUsuarioId(1L);
        dto.setTipo(TipoSoporte.OTRO);

        when(soporteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<Soporte> response = soporteService.crear(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getEstado()).isEqualTo(EstadoSoporte.PENDIENTE);
    }

    @Test
    void actualizarEstado_toResuelto_setsResolucionDate() {
        Soporte soporte = Soporte.builder().id(1L).estado(EstadoSoporte.PENDIENTE).build();
        when(soporteRepository.findById(1L)).thenReturn(Optional.of(soporte));
        when(soporteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<Soporte> response = soporteService.actualizarEstado(1L, EstadoSoporte.RESUELTO);

        assertThat(response.getBody().getEstado()).isEqualTo(EstadoSoporte.RESUELTO);
        assertThat(response.getBody().getFechaResolucion()).isNotNull();
    }

    @Test
    void asignar_validTicket_setsVoluntarioAndEnProgreso() {
        Soporte soporte = Soporte.builder().id(1L).estado(EstadoSoporte.PENDIENTE).build();
        when(soporteRepository.findById(1L)).thenReturn(Optional.of(soporte));
        when(soporteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<Soporte> response = soporteService.asignar(1L, 5L);

        assertThat(response.getBody().getAsignadoA()).isEqualTo(5L);
        assertThat(response.getBody().getEstado()).isEqualTo(EstadoSoporte.EN_PROGRESO);
    }

    @Test
    void validateCampaign_approved_publishesActivatedEvent() {
        Soporte soporte = Soporte.builder().id(1L).campaignId(10L).estado(EstadoSoporte.PENDIENTE).build();
        when(soporteRepository.findById(1L)).thenReturn(Optional.of(soporte));
        when(soporteRepository.save(any())).thenReturn(soporte);

        ResponseEntity<MessageResponse> response = soporteService.validateCampaign(1L, true, "OK");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(soporte.getEstado()).isEqualTo(EstadoSoporte.RESUELTO);
        verify(campaignResultPublisher).publish(argThat(e -> e.approved()));
    }

    @Test
    void validateCampaign_rejected_publishesRejectedEvent() {
        Soporte soporte = Soporte.builder().id(1L).campaignId(10L).estado(EstadoSoporte.PENDIENTE).build();
        when(soporteRepository.findById(1L)).thenReturn(Optional.of(soporte));
        when(soporteRepository.save(any())).thenReturn(soporte);

        soporteService.validateCampaign(1L, false, "Falta documentación");

        assertThat(soporte.getEstado()).isEqualTo(EstadoSoporte.CERRADO);
        verify(campaignResultPublisher).publish(argThat(e -> !e.approved()));
    }

    @Test
    void validateTransfer_approved_publishesValidatedEvent() {
        Soporte soporte = Soporte.builder().id(1L).donationId(20L).estado(EstadoSoporte.PENDIENTE).build();
        when(soporteRepository.findById(1L)).thenReturn(Optional.of(soporte));
        when(soporteRepository.save(any())).thenReturn(soporte);

        ResponseEntity<MessageResponse> response = soporteService.validateTransfer(1L, true, "Transferencia correcta");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(transferResultPublisher).publish(argThat(e -> e.approved()));
    }

    @Test
    void validateTransfer_rejected_publishesRejectedEvent() {
        Soporte soporte = Soporte.builder().id(1L).donationId(20L).estado(EstadoSoporte.PENDIENTE).build();
        when(soporteRepository.findById(1L)).thenReturn(Optional.of(soporte));
        when(soporteRepository.save(any())).thenReturn(soporte);

        soporteService.validateTransfer(1L, false, "Monto incorrecto");

        verify(transferResultPublisher).publish(argThat(e -> !e.approved()));
    }

    @Test
    void getByEstado_returnsFilteredList() {
        Soporte s = Soporte.builder().estado(EstadoSoporte.EN_PROGRESO).build();
        when(soporteRepository.findByEstado(EstadoSoporte.EN_PROGRESO)).thenReturn(List.of(s));

        List<Soporte> result = soporteService.getByEstado(EstadoSoporte.EN_PROGRESO);

        assertThat(result).allMatch(x -> x.getEstado() == EstadoSoporte.EN_PROGRESO);
    }

    @Test
    void getByTipo_returnsFilteredList() {
        Soporte s = Soporte.builder().tipo(TipoSoporte.DONACION).build();
        when(soporteRepository.findByTipo(TipoSoporte.DONACION)).thenReturn(List.of(s));

        List<Soporte> result = soporteService.getByTipo(TipoSoporte.DONACION);

        assertThat(result).allMatch(x -> x.getTipo() == TipoSoporte.DONACION);
    }
}
