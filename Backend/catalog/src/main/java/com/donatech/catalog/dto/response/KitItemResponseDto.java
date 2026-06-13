package com.donatech.catalog.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class KitItemResponseDto {
    Long id;
    String productId;
    String productNombre;
    Integer productPrecio;
    Integer cantidadRequerida;
    Boolean productHasImage;
}
