package com.donatech.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

// Edición administrativa de un usuario. NO incluye contraseña (se conserva la actual).
@Data
public class AdminUserUpdateDto {

    @NotEmpty(message = "El nombre no puede estar vacío")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ]+$", message = "El nombre solo puede contener letras, sin espacios")
    @Size(min = 2, max = 100)
    private String name;

    @NotEmpty(message = "El apellido no puede estar vacío")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ]+$", message = "El apellido solo puede contener letras, sin espacios")
    @Size(min = 2, max = 100)
    private String apellido;

    @NotEmpty(message = "El correo no puede estar vacío")
    @Email(message = "Correo inválido")
    private String email;

    @NotNull(message = "El rol es obligatorio")
    private Long roleId;

    @NotNull(message = "El estado es obligatorio")
    @Min(0) @Max(1)
    private Integer status;
}
