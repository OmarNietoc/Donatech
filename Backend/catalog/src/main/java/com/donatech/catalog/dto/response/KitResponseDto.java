package com.donatech.catalog.dto.response;

import com.donatech.catalog.model.KitTipo;
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
    KitTipo tipo;
    Integer precioEstimado;
    List<KitItemResponseDto> items;
    Boolean hasImage;
    // Solo para kits USO_UNICO: campaña a la que pertenecen.
    Long campaignId;
    String campaignTitulo;
}
