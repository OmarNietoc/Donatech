package com.donatech.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CampaignRequestDto {

    @NotNull(message = "El ID del beneficiario es obligatorio")
    private Long beneficiaryId;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200)
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 100)
    private String motivo;

    private Long regionId;
    private Long comunaId;

    @Size(max = 2000)
    private String observaciones;

    private List<CampaignKitDto> kits;
}
