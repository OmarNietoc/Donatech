package com.donatech.order.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductResponseDto {
    private String id;
    private String nombre;
    private String descripcion;
    private Integer precio;
    private Integer stock;
    private Integer stockMinimo;
    private Integer activo;
    private Long categoriaId;
    private Long unidadId;
    private byte[] imagen;
}
