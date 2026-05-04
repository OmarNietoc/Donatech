package com.donatech.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Datos para registrar un beneficiario")
public class BeneficiaryDto {
    @NotNull
    @Schema(description = "ID del usuario asociado")
    private Long userId;

    @NotBlank
    @Schema(description = "RUT chileno del beneficiario", example = "12345678-9")
    private String rut;

    @Schema(description = "Dirección de entrega de donaciones")
    private String direccionEntrega;
    @Schema(description = "ID del voluntario que registra (si aplica)")
    private Long registeredById;
    @Schema(description = "Motivo del registro (obligatorio si registeredById está presente)")
    private String motivoRegistro;
    private String observaciones;
}
