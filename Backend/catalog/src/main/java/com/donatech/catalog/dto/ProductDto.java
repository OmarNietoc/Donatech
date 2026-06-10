package com.donatech.catalog.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {

    @NotBlank(message = "El id es obligatorio")
    private String id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres")
    private String nombre;

    @NotBlank(message = "La descripcion es obligatoria")
    @Size(min = 20, max = 1000, message = "La descripcion debe tener entre 20 y 1000 caracteres")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @PositiveOrZero
    private Integer precio;

    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero
    private Integer stock;

    @PositiveOrZero
    private Integer stockMinimo;

    @Min(0)
    @Max(1)
    private Integer activo;

    @NotNull(message = "El ID de la categoria es obligatorio")
    private Long categoriaId;

    @NotNull(message = "El ID de la unidad es obligatorio")
    private Long unidadId;

    private com.donatech.catalog.model.Prioridad prioridad;
}
