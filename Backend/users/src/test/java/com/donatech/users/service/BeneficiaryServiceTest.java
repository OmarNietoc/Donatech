package com.donatech.users.service;

import com.donatech.users.dto.BeneficiaryDto;
import com.donatech.users.event.BeneficiaryEventPublisher;
import com.donatech.users.exception.ConflictException;
import com.donatech.users.exception.ResourceNotFoundException;
import com.donatech.users.model.Beneficiary;
import com.donatech.users.model.EstadoVerificacion;
import com.donatech.users.model.User;
import com.donatech.users.repository.BeneficiaryRepository;
import com.donatech.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeneficiaryServiceTest {

    @Mock BeneficiaryRepository beneficiaryRepository;
    @Mock UserRepository userRepository;
    @Mock BeneficiaryEventPublisher beneficiaryEventPublisher;

    @InjectMocks BeneficiaryService beneficiaryService;

    @Test
    void getAll_returnsAllBeneficiaries() {
        Beneficiary b = new Beneficiary();
        when(beneficiaryRepository.findAll()).thenReturn(List.of(b));

        List<Beneficiary> result = beneficiaryService.getAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void getById_existingId_returnsBeneficiary() {
        Beneficiary b = new Beneficiary();
        b.setEstadoVerificacion(EstadoVerificacion.PENDIENTE);
        when(beneficiaryRepository.findById(1L)).thenReturn(Optional.of(b));

        Beneficiary result = beneficiaryService.getById(1L);

        assertThat(result).isNotNull();
    }

    @Test
    void getById_nonExistingId_throwsException() {
        when(beneficiaryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> beneficiaryService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_validDto_savesBeneficiary() {
        BeneficiaryDto dto = new BeneficiaryDto();
        dto.setRut("11111111-1");
        dto.setUserId(1L);
        dto.setDireccionEntrega("Calle 123");

        User user = new User();
        user.setId(1L);

        when(beneficiaryRepository.existsByRut(any())).thenReturn(false);
        when(beneficiaryRepository.existsByUserId(1L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(beneficiaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Beneficiary result = beneficiaryService.create(dto);

        assertThat(result.getEstadoVerificacion()).isEqualTo(EstadoVerificacion.PENDIENTE);
        verify(beneficiaryRepository).save(any(Beneficiary.class));
    }

    @Test
    void create_duplicateRut_throwsConflictException() {
        BeneficiaryDto dto = new BeneficiaryDto();
        dto.setRut("11111111-1");
        dto.setUserId(1L);

        when(beneficiaryRepository.existsByRut(any())).thenReturn(true);

        assertThatThrownBy(() -> beneficiaryService.create(dto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void verify_approves_setsVerifiedState() {
        User verificador = new User();
        verificador.setId(2L);
        User user = new User();
        user.setId(1L);

        Beneficiary b = new Beneficiary();
        b.setUser(user);
        b.setEstadoVerificacion(EstadoVerificacion.PENDIENTE);

        when(beneficiaryRepository.findById(1L)).thenReturn(Optional.of(b));
        when(userRepository.findById(2L)).thenReturn(Optional.of(verificador));
        when(beneficiaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Beneficiary result = beneficiaryService.verify(1L, EstadoVerificacion.VERIFICADO, 2L, null);

        assertThat(result.getEstadoVerificacion()).isEqualTo(EstadoVerificacion.VERIFICADO);
        verify(beneficiaryEventPublisher).publishBeneficiaryVerified(any());
    }

    @Test
    void verify_rejects_setsRejectedState() {
        User verificador = new User();
        verificador.setId(2L);
        User user = new User();
        user.setId(1L);

        Beneficiary b = new Beneficiary();
        b.setUser(user);
        b.setEstadoVerificacion(EstadoVerificacion.PENDIENTE);

        when(beneficiaryRepository.findById(1L)).thenReturn(Optional.of(b));
        when(userRepository.findById(2L)).thenReturn(Optional.of(verificador));
        when(beneficiaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Beneficiary result = beneficiaryService.verify(1L, EstadoVerificacion.RECHAZADO, 2L, "Documentación incompleta");

        assertThat(result.getEstadoVerificacion()).isEqualTo(EstadoVerificacion.RECHAZADO);
        assertThat(result.getMotivoRechazo()).isEqualTo("Documentación incompleta");
        verify(beneficiaryEventPublisher, never()).publishBeneficiaryVerified(any());
    }

    @Test
    void delete_existingId_deletesFromRepository() {
        Beneficiary b = new Beneficiary();
        when(beneficiaryRepository.findById(1L)).thenReturn(Optional.of(b));

        beneficiaryService.delete(1L);

        verify(beneficiaryRepository).deleteById(1L);
    }
}
