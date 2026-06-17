package com.donatech.users.dto;

/**
 * Datos de contacto de un usuario para notificaciones de entrega.
 * direccion proviene de Beneficiary.direccionEntrega cuando el usuario es beneficiario.
 */
public record ContactDto(
        Long id,
        String name,
        String apellido,
        String email,
        String phone,
        String direccion,
        String comuna,
        String region
) {}
