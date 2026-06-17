package com.donatech.shipping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos para crear una nueva ruta de entrega")
public class RouteCreationRequestDTO {
    @Schema(description = "ID de la empresa transportista")
    private String companyId;
    @Schema(description = "ID del transportista (ej: 'DHL' o 'LOCAL')")
    private String carrierId;
    @Schema(description = "ID del colaborador (voluntario) asignado")
    private Long collaboratorId;
    @Schema(description = "Nombre del colaborador asignado")
    private String collaboratorNombre;
    @Schema(description = "Email del colaborador asignado")
    private String collaboratorEmail;
    @Schema(description = "Dirección de origen para geocodificación", example = "Av. Providencia 1234, Santiago")
    private String originAddress;
    @Schema(description = "Lista de IDs de envíos a incluir en la ruta")
    private List<String> shipmentIds;
    @Schema(description = "true = optimizar con OSRM, false = orden manual", defaultValue = "false")
    @Builder.Default
    private boolean optimizeRoute = false;
}

