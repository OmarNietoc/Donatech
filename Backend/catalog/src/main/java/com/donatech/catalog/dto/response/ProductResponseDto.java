package com.donatech.catalog.dto.response;

import com.donatech.catalog.model.Prioridad;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductResponseDto {
    String id;
    String nombre;
    String descripcion;
    Integer precio;
    Integer stock;
    Integer stockMinimo;
    Integer activo;
    Prioridad prioridad;
    Long categoriaId;
    String categoriaNombre;
    Long unidadId;
    String unidadNombre;
    Boolean hasImage;
}
