package com.donatech.users.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

// Actualización del perfil propio. NO incluye email, rut, rol, estado ni contraseña.
@Data
public class ProfileUpdateDto {

    @NotEmpty(message = "El nombre no puede estar vacío")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ]+$", message = "El nombre solo puede contener letras, sin espacios")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String name;

    @NotEmpty(message = "El apellido no puede estar vacío")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ]+$", message = "El apellido solo puede contener letras, sin espacios")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    private String apellido;

    private String phone;
    private Long regionId;
    private Long comunaId;

    // Beneficiario
    @Size(max = 300)
    private String direccionEntrega;
    @Size(max = 1000)
    private String observaciones;

    // Organización (empresa)
    @Size(max = 200)
    private String razonSocial;
    @Size(max = 200)
    private String giro;
    @Size(max = 400)
    private String direccionLegal;
}
