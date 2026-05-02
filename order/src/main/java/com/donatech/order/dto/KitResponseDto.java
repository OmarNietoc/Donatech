package com.donatech.order.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KitResponseDto {
    private Long id;
    private String nombre;
    private Integer precioEstimado;
    private Integer activo;
}
