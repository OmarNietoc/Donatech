package com.donatech.auth.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterBeneficiaryRequest {

    @NotBlank @Size(min = 4, max = 100)
    private String name;

    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 6)
    private String password;

    private String phone;
    private Long regionId;
    private Long comunaId;

    @NotBlank
    private String rut;

    private String direccionEntrega;
    private String observaciones;
}
