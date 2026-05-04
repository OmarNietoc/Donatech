package com.donatech.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "Datos para registrar un nuevo usuario en la plataforma")
public class RegisterRequest {

    @NotBlank
    @Size(min = 4, max = 100)
    @Schema(description = "Nombre completo del usuario", example = "Juan Pérez")
    private String name;

    @NotBlank
    @Email
    @Schema(description = "Correo electrónico único", example = "juan@donatech.cl")
    private String email;

    @NotBlank
    @Size(min = 6)
    @Schema(description = "Contraseña (mínimo 6 caracteres)")
    private String password;

    @NotNull
    @Schema(description = "ID del rol asignado (1=ADMIN, 2=DONANTE, 3=VOLUNTARIO, etc.)")
    private Long roleId;

    @Schema(description = "Teléfono de contacto")
    private String phone;
    private Long regionId;
    private Long comunaId;
}
