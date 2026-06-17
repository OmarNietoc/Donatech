package com.donatech.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Crear una donación: un pago que agrupa una orden por campaña.
 * Cada grupo = una campaña con sus kits.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos para crear una donación multi-campaña (una orden por grupo)")
public class DonationDto {

    @NotBlank(message = "El email del usuario es obligatorio")
    @Email(message = "El email del usuario debe ser válido")
    private String userEmail;

    private String couponCode;

    @NotEmpty(message = "La donación debe contener al menos una campaña")
    private List<@Valid Group> groups;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Kits de una campaña dentro de la donación")
    public static class Group {
        @NotNull(message = "El ID de campaña es obligatorio")
        private Long campaignId;

        @NotEmpty(message = "El grupo debe contener al menos un kit")
        private List<@Valid OrderItemRequestDto> items;
    }
}
