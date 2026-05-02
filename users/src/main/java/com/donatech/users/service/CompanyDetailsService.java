package com.donatech.users.service;

import com.donatech.users.dto.CompanyDetailsDto;
import com.donatech.users.exception.ConflictException;
import com.donatech.users.exception.ResourceNotFoundException;
import com.donatech.users.model.CompanyDetails;
import com.donatech.users.model.User;
import com.donatech.users.repository.CompanyDetailsRepository;
import com.donatech.users.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyDetailsService {

    private final CompanyDetailsRepository companyDetailsRepository;
    private final UserRepository userRepository;

    public ResponseEntity<CompanyDetails> createOrUpdate(@Valid CompanyDetailsDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + dto.getUserId()));

        CompanyDetails details = companyDetailsRepository.findByUserId(dto.getUserId())
                .orElse(CompanyDetails.builder().user(user).build());

        if (details.getId() == null && companyDetailsRepository.existsByRut(dto.getRut())) {
            throw new ConflictException("El RUT ya está registrado.");
        }

        details.setRut(dto.getRut());
        details.setRazonSocial(dto.getRazonSocial());
        details.setGiro(dto.getGiro());
        details.setDireccionLegal(dto.getDireccionLegal());

        HttpStatus status = (details.getId() == null) ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(companyDetailsRepository.save(details));
    }

    public ResponseEntity<CompanyDetails> getByUserId(Long userId) {
        CompanyDetails details = companyDetailsRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Datos de empresa no encontrados para usuario: " + userId));
        return ResponseEntity.ok(details);
    }
}
