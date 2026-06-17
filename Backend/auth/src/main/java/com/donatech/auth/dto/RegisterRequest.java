package com.donatech.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "Datos para registrar un nuevo usuario en la plataforma")
public class RegisterRequest {

    @NotBlank
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ]+$", message = "El nombre solo puede contener letras, sin espacios")
    @Size(min = 2, max = 100)
    @Schema(description = "Nombre del usuario", example = "Juan")
    private String name;

    @NotBlank
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ]+$", message = "El apellido solo puede contener letras, sin espacios")
    @Size(min = 2, max = 100)
    @Schema(description = "Apellido del usuario", example = "Pérez")
    private String apellido;

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
