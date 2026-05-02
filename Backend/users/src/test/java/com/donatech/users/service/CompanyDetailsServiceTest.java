package com.donatech.users.service;

import com.donatech.users.dto.CompanyDetailsDto;
import com.donatech.users.exception.ConflictException;
import com.donatech.users.exception.ResourceNotFoundException;
import com.donatech.users.model.CompanyDetails;
import com.donatech.users.model.User;
import com.donatech.users.repository.CompanyDetailsRepository;
import com.donatech.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyDetailsServiceTest {

    @Mock CompanyDetailsRepository companyDetailsRepository;
    @Mock UserRepository userRepository;

    @InjectMocks CompanyDetailsService companyDetailsService;

    private CompanyDetailsDto buildDto() {
        CompanyDetailsDto dto = new CompanyDetailsDto();
        dto.setUserId(1L);
        dto.setRut("76123456-7");
        dto.setRazonSocial("Empresa Test SpA");
        dto.setGiro("Comercio");
        dto.setDireccionLegal("Av. Principal 100");
        return dto;
    }

    @Test
    void createOrUpdate_newRecord_createsWithStatus201() {
        CompanyDetailsDto dto = buildDto();
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(companyDetailsRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(companyDetailsRepository.existsByRut("76123456-7")).thenReturn(false);
        when(companyDetailsRepository.save(any())).thenAnswer(inv -> {
            CompanyDetails cd = inv.getArgument(0);
            cd.setId(1L);
            return cd;
        });

        ResponseEntity<CompanyDetails> response = companyDetailsService.createOrUpdate(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(companyDetailsRepository).save(any(CompanyDetails.class));
    }

    @Test
    void createOrUpdate_existingRecord_updatesWithStatus200() {
        CompanyDetailsDto dto = buildDto();
        User user = new User();
        user.setId(1L);

        CompanyDetails existing = new CompanyDetails();
        existing.setId(5L);
        existing.setUser(user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(companyDetailsRepository.findByUserId(1L)).thenReturn(Optional.of(existing));
        when(companyDetailsRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<CompanyDetails> response = companyDetailsService.createOrUpdate(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getRazonSocial()).isEqualTo("Empresa Test SpA");
    }

    @Test
    void createOrUpdate_duplicateRut_throwsConflict() {
        CompanyDetailsDto dto = buildDto();
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(companyDetailsRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(companyDetailsRepository.existsByRut("76123456-7")).thenReturn(true);

        assertThatThrownBy(() -> companyDetailsService.createOrUpdate(dto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void getByUserId_exists_returnsDetails() {
        CompanyDetails details = new CompanyDetails();
        details.setRazonSocial("Org Solidaria");
        when(companyDetailsRepository.findByUserId(1L)).thenReturn(Optional.of(details));

        ResponseEntity<CompanyDetails> response = companyDetailsService.getByUserId(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getRazonSocial()).isEqualTo("Org Solidaria");
    }

    @Test
    void getByUserId_notFound_throwsException() {
        when(companyDetailsRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyDetailsService.getByUserId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
