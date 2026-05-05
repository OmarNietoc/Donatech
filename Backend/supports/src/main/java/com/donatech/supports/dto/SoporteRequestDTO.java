package com.donatech.supports.dto;

import com.donatech.supports.model.PrioridadSoporte;
import com.donatech.supports.model.TipoSoporte;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Datos para crear un ticket de soporte")
public class SoporteRequestDTO {

    @NotBlank(message = "La descripción no puede estar vacía")
    @Schema(description = "Descripción detallada del problema")
    private String descripcion;

    @Schema(description = "Título breve del ticket")
    private String titulo;

    @NotNull(message = "El usuarioId es obligatorio")
    @Schema(description = "ID del usuario que abre el ticket")
    private Long usuarioId;

    @NotNull(message = "La prioridad es obligatoria")
    @Schema(description = "Prioridad: BAJA, MEDIA, ALTA")
    private PrioridadSoporte prioridad;

    @NotNull(message = "El tipo es obligatorio")
    @Schema(description = "Tipo: GENERAL, VALIDACION_CAMPAÑA, VALIDACION_TRANSFERENCIA")
    private TipoSoporte tipo;

    @Schema(description = "ID de la donación relacionada (opcional)")
    private Long donationId;

    @Schema(description = "ID de la campaña relacionada (opcional)")
    private Long campaignId;

    @Schema(description = "Email del destinatario para notificaciones (opcional)")
    private String recipientEmail;
}
