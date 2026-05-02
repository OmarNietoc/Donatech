package com.donatech.catalog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NecesidadZonaDto {

    @NotBlank(message = "El ID del producto es obligatorio")
    private String productoId;

    @NotNull(message = "El ID de la comuna es obligatorio")
    private Long comunaId;

    @NotNull
    @Min(0)
    private Integer cantidadNecesaria;

    @Min(0)
    private Integer cantidadCubierta;
}
