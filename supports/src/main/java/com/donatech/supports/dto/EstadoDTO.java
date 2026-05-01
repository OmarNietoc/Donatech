package com.donatech.supports.dto;

import com.donatech.supports.model.EstadoSoporte;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EstadoDTO {
    @NotNull(message = "El estado es obligatorio")
    private EstadoSoporte estado;
}
