package com.donatech.users.service;

import com.donatech.users.dto.BeneficiaryDto;
import com.donatech.users.event.BeneficiaryEventPublisher;
import com.donatech.users.event.BeneficiaryVerifiedEvent;
import com.donatech.users.exception.ConflictException;
import com.donatech.users.exception.ResourceNotFoundException;
import com.donatech.users.model.Beneficiary;
import com.donatech.users.model.EstadoVerificacion;
import com.donatech.users.model.User;
import com.donatech.users.repository.BeneficiaryRepository;
import com.donatech.users.repository.UserRepository;
import com.donatech.users.util.RutValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;
    private final BeneficiaryEventPublisher beneficiaryEventPublisher;

    public List<Beneficiary> getAll() {
        return beneficiaryRepository.findAll();
    }

    public List<Beneficiary> getByEstado(EstadoVerificacion estado) {
        return beneficiaryRepository.findByEstadoVerificacion(estado);
    }

    public Beneficiary getById(Long id) {
        return beneficiaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiario no encontrado: " + id));
    }

    public Beneficiary getByUserId(Long userId) {
        return beneficiaryRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de beneficiario no encontrado para usuario: " + userId));
    }

    public Beneficiary create(BeneficiaryDto dto) {
        if (!RutValidator.isValid(dto.getRut())) {
            throw new IllegalArgumentException("RUT inválido: " + dto.getRut());
        }
        String rutNormalized = RutValidator.normalize(dto.getRut());
        if (beneficiaryRepository.existsByRut(rutNormalized)) {
            throw new ConflictException("RUT ya registrado: " + rutNormalized);
        }
        if (beneficiaryRepository.existsByUserId(dto.getUserId())) {
            throw new ConflictException("Usuario ya tiene perfil de beneficiario");
        }

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + dto.getUserId()));

        User registeredBy = null;
        if (dto.getRegisteredById() != null) {
            registeredBy = userRepository.findById(dto.getRegisteredById())
                    .orElseThrow(() -> new ResourceNotFoundException("Registrador no encontrado"));
            if (dto.getMotivoRegistro() == null || dto.getMotivoRegistro().isBlank()) {
                throw new IllegalArgumentException("motivoRegistro es obligatorio cuando registeredById está presente");
            }
        }

        Beneficiary b = new Beneficiary();
        b.setUser(user);
        b.setRut(rutNormalized);
        b.setEstadoVerificacion(EstadoVerificacion.PENDIENTE);
        b.setDireccionEntrega(dto.getDireccionEntrega());
        b.setRegisteredBy(registeredBy);
        b.setMotivoRegistro(dto.getMotivoRegistro());
        b.setObservaciones(dto.getObservaciones());
        b.setFechaRegistro(LocalDateTime.now());

        return beneficiaryRepository.save(b);
    }

    public Beneficiary verify(Long id, EstadoVerificacion nuevoEstado, Long verificadorId, String motivoRechazo) {
        Beneficiary b = getById(id);
        User verificador = userRepository.findById(verificadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Verificador no encontrado: " + verificadorId));

        b.setEstadoVerificacion(nuevoEstado);
        b.setVerificadoPor(verificador);
        b.setFechaVerificacion(LocalDateTime.now());
        if (nuevoEstado == EstadoVerificacion.RECHAZADO) {
            b.setMotivoRechazo(motivoRechazo);
        }
        Beneficiary saved = beneficiaryRepository.save(b);

        if (nuevoEstado == EstadoVerificacion.VERIFICADO) {
            beneficiaryEventPublisher.publishBeneficiaryVerified(new BeneficiaryVerifiedEvent(
                    saved.getId(), saved.getUser().getId(), saved.getRut(), saved.getFechaVerificacion()
            ));
        }

        return saved;
    }

    public void delete(Long id) {
        getById(id);
        beneficiaryRepository.deleteById(id);
    }
}
