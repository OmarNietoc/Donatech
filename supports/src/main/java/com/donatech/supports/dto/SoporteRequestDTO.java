package com.donatech.supports.dto;

import com.donatech.supports.model.PrioridadSoporte;
import com.donatech.supports.model.TipoSoporte;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SoporteRequestDTO {

    @NotBlank(message = "La descripción no puede estar vacía")
    private String descripcion;

    private String titulo;

    @NotNull(message = "El usuarioId es obligatorio")
    private Long usuarioId;

    @NotNull(message = "La prioridad es obligatoria")
    private PrioridadSoporte prioridad;

    @NotNull(message = "El tipo es obligatorio")
    private TipoSoporte tipo;

    private Long donationId;
    private Long campaignId;
}
