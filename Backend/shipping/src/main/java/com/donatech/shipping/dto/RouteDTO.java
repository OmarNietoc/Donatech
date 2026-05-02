package com.donatech.shipping.dto;

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
public class RouteDTO {
    private String id;
    private String companyId;
    private String carrierId;
    private LocalDate routeDate;
    private String originAddress;
    private String optimizedPathJson;
    private String status;
    private List<ShipmentDTO> shipments;
}

