package com.donatech.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BeneficiaryDto {
    @NotNull
    private Long userId;

    @NotBlank
    private String rut;

    private String direccionEntrega;
    private Long registeredById;
    private String motivoRegistro;
    private String observaciones;
}
