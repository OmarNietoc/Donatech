package com.donatech.catalog.dto.response;

import com.donatech.catalog.model.CampaignStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class CampaignResponseDto {
    Long id;
    Long beneficiaryId;
    String titulo;
    String descripcion;
    String motivo;
    CampaignStatus estado;
    Long regionId;
    Long comunaId;
    Integer costoLogistica;
    LocalDateTime fechaCreacion;
    LocalDateTime fechaActivacion;
    LocalDateTime fechaCierre;
    String observaciones;
    String motivoRechazo;
    List<CampaignKitResponseDto> kits;
}
