package com.donatech.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompanyDetailsDto {

    @NotNull(message = "El userId es obligatorio")
    private Long userId;

    @NotBlank(message = "El RUT es obligatorio")
    @Pattern(regexp = "\\d{7,8}-[\\dkK]", message = "RUT inválido (ej: 12345678-9)")
    private String rut;

    @NotBlank(message = "La razón social es obligatoria")
    @Size(max = 200)
    private String razonSocial;

    @Size(max = 200)
    private String giro;

    @Size(max = 400)
    private String direccionLegal;
}
