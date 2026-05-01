package com.donatech.supports.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResponderDTO {
    @NotBlank(message = "La respuesta no puede estar vacía")
    private String respuesta;
}
