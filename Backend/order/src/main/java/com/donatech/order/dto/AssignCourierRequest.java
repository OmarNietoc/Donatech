package com.donatech.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AssignCourierRequest {

    @NotBlank(message = "El nombre del transportista es obligatorio")
    @Size(max = 150)
    private String transportistaNombre;

    @Size(max = 100)
    private String transportistaContacto;
}
