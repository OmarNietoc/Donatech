package com.donatech.catalog.dto.response;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class KitResponseDto {
    Long id;
    String nombre;
    String descripcion;
    Integer activo;
    Integer precioEstimado;
    List<KitItemResponseDto> items;
    Boolean hasImage;
}
