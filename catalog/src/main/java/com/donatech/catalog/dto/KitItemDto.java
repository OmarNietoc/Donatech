package com.donatech.catalog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KitItemDto {

    @NotBlank(message = "El ID del producto es obligatorio")
    private String productId;

    @NotNull
    @Min(1)
    private Integer cantidadRequerida;
}
