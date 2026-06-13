package com.donatech.users.dto;

/**
 * Resumen de usuario para consumo entre microservicios.
 * Incluye alias en español (nombre/correo) porque algunos consumidores
 * (supports) deserializan con esos nombres de campo.
 */
public record UserSummaryDto(
        Long id,
        String name,
        String email,
        String nombre,
        String correo,
        Integer status
) {
    public static UserSummaryDto of(Long id, String name, String email, Integer status) {
        return new UserSummaryDto(id, name, email, name, email, status);
    }
}
