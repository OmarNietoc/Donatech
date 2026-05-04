package com.donatech.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos para crear o actualizar una orden de donación")
public class OrderDto {

    @NotBlank(message = "El email del usuario es obligatorio")
    @Email(message = "El email del usuario debe ser válido")
    private String userEmail;

    private Long campaignId;

    @NotEmpty(message = "La orden debe contener al menos un kit")
    private List<@Valid OrderItemRequestDto> items;

    private String couponCode;
}
