package com.donatech.shipping.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class RouteReorderRequestDTO {
    @NotEmpty(message = "La lista de envíos no puede estar vacía")
    private List<String> shipmentIds;
}
