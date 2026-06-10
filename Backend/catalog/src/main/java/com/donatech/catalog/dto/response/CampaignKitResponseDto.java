package com.donatech.catalog.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CampaignKitResponseDto {
    Long id;
    Long kitId;
    String kitNombre;
    Integer kitPrecioEstimado;
    Integer cantidadNecesaria;
    Integer cantidadFulfilled;
}
