package com.donatech.shipping.dto;

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
public class ShipmentDTO {
    private String id;
    private String orderId;
    private String routeId;
    private String customerName;
    private String customerEmail;
    private String shippingAddress;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String trackingNumber;
    private String deliveryStatus;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime actualDelivery;
}

