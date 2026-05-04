package com.donatech.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ítem de kit dentro de una orden de donación")
public class OrderItemRequestDto {

    @NotNull(message = "El ID del kit es obligatorio")
    @Schema(description = "ID del kit en catalog ms", example = "1")
    private Long kitId;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que 0")
    @Schema(description = "Cantidad de kits a donar", example = "2")
    private Integer quantity;
}
