package com.donatech.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.ScriptAssert;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para la creación y actualización de usuarios")
public class UserDto {

    @Schema(description = "Nombre del usuario", example = "1")
    @NotEmpty(message = "'name' no puede estar vacío")
    @Size(min = 4, max = 100, message = "'name' debe tener entre 4 y 100 caracteres")
    private String name;

    @Email(message = "'email' debe ser válido")
    @NotEmpty(message = "'email' no puede estar vacío")
    private String email;

    @NotEmpty(message = "'password' no puede estar vacía")
    @Size(min = 6, message = "'password' debe tener al menos 6 caracteres")
    private String password;

    @NotNull(message = "'roleId' no puede ser nulo")
    private Long role;

    @NotNull(message = "'status' no puede ser nulo")
    @Min(value = 0, message = "El 'status' debe ser 1 o 0")
    @Max(value = 1, message = "El 'status' debe ser 1 o 0")
    private Integer status;

    // Imagen de perfil (nullable)
    @Lob
    @Column(name = "imagen", columnDefinition = "LONGBLOB", nullable = true)
    private byte[] imagen;

    private String phone;
    private Long region;
    private Long comuna;
}
