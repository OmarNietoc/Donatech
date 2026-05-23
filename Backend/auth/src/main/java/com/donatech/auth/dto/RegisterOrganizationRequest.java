package com.donatech.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "Datos para registrar una organización: campos de usuario + datos de empresa")
public class RegisterOrganizationRequest {

    @NotBlank @Size(min = 4, max = 100)
    private String name;

    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 6)
    private String password;

    private String phone;
    private Long regionId;
    private Long comunaId;

    @NotBlank(message = "El RUT de la empresa es obligatorio")
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
