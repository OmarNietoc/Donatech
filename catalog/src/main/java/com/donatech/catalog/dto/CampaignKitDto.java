package com.donatech.catalog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CampaignKitDto {

    @NotNull(message = "El ID del kit es obligatorio")
    private Long kitId;

    @NotNull
    @Min(1)
    private Integer cantidadNecesaria;
}
