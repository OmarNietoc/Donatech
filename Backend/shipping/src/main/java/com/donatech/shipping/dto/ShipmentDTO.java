package com.donatech.shipping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos de un envío individual")
public class ShipmentDTO {
    @Schema(description = "ID UUID del envío")
    private String id;
    @Schema(description = "ID de la orden asociada")
    private String orderId;
    @Schema(description = "ID de la ruta asignada (nullable)")
    private String routeId;
    @Schema(description = "Nombre del destinatario")
    private String customerName;
    @Schema(description = "Email del destinatario")
    private String customerEmail;
    @Schema(description = "Dirección de entrega")
    private String shippingAddress;
    @Schema(description = "Latitud para optimización de ruta")
    private BigDecimal latitude;
    @Schema(description = "Longitud para optimización de ruta")
    private BigDecimal longitude;
    @Schema(description = "Número de seguimiento único")
    private String trackingNumber;
    @Schema(description = "Estado del envío", example = "PENDING")
    private String deliveryStatus;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime actualDelivery;
}

