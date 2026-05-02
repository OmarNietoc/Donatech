package com.donatech.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateBeneficiaryInternalDto {
    private Long userId;
    private String rut;
    private String direccionEntrega;
    private String observaciones;
}
