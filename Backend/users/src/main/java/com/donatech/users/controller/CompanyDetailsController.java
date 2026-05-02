package com.donatech.users.controller;

import com.donatech.users.dto.CompanyDetailsDto;
import com.donatech.users.model.CompanyDetails;
import com.donatech.users.service.CompanyDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/company")
@RequiredArgsConstructor
@Tag(name = "CompanyDetails", description = "Datos de empresa para organizaciones y beneficiarios")
public class CompanyDetailsController {

    private final CompanyDetailsService companyDetailsService;

    @Operation(summary = "Crear o actualizar datos de empresa")
    @PostMapping
    public ResponseEntity<CompanyDetails> createOrUpdate(@Valid @RequestBody CompanyDetailsDto dto) {
        return companyDetailsService.createOrUpdate(dto);
    }

    @Operation(summary = "Obtener datos de empresa por userId")
    @GetMapping("/{userId}")
    public ResponseEntity<CompanyDetails> getByUserId(@PathVariable Long userId) {
        return companyDetailsService.getByUserId(userId);
    }
}
