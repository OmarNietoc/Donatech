package com.donatech.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateCompanyInternalDto {
    private Long userId;
    private String rut;
    private String razonSocial;
    private String giro;
    private String direccionLegal;
}
