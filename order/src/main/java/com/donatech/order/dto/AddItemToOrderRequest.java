package com.donatech.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddItemToOrderRequest {

    @NotBlank(message = "El email del usuario es obligatorio")
    @Email(message = "El email del usuario debe ser válido")
    private String userEmail;

    private Long campaignId;

    @Valid
    private OrderItemRequestDto item;
}
