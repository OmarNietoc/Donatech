package com.donatech.order.dto;

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
public class OrderDto {

    @NotBlank(message = "El email del usuario es obligatorio")
    @Email(message = "El email del usuario debe ser válido")
    private String userEmail;

    @NotEmpty(message = "La orden debe contener al menos un producto")
    private List<@Valid OrderItemRequestDto> items;

    // Código de cupón opcional
    private String couponCode;
}
