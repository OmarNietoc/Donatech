package com.donatech.shipping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Ruta de entrega con paradas optimizadas")
public class RouteDTO {
    @Schema(description = "ID UUID de la ruta")
    private String id;
    @Schema(description = "ID de la empresa")
    private String companyId;
    @Schema(description = "ID del transportista (ej: DHL, LOCAL)")
    private String carrierId;
    private LocalDate routeDate;
    @Schema(description = "Dirección de origen de la ruta")
    private String originAddress;
    @Schema(description = "JSON con resultado de optimización OSRM o manual")
    private String optimizedPathJson;
    @Schema(description = "Estado de la ruta: PLANNED, IN_PROGRESS, COMPLETED, CANCELLED")
    private String status;
    private List<ShipmentDTO> shipments;
}

