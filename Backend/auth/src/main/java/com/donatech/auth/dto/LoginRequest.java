package com.donatech.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Credenciales para autenticación")
public class LoginRequest {
    @NotBlank
    @Email
    @Schema(description = "Correo electrónico del usuario", example = "usuario@donatech.cl")
    private String email;

    @NotBlank
    @Schema(description = "Contraseña del usuario", example = "pass1234")
    private String password;
}
