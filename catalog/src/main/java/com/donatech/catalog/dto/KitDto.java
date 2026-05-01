package com.donatech.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KitDto {

    @NotBlank(message = "El nombre del kit es obligatorio")
    @Size(min = 3, max = 120)
    private String nombre;

    @Size(max = 1000)
    private String descripcion;

    private byte[] imagen;

    private Integer activo;

    private Integer precioEstimado;

    private List<KitItemDto> items;
}
