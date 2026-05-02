package com.donatech.shipping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteCreationRequestDTO {
    private String companyId;
    private String carrierId;
    private String originAddress;
    private List<String> shipmentIds;
    @Builder.Default
    private boolean optimizeRoute = true;
}

