package com.donatech.users.dto;

// Perfil propio del usuario autenticado. Los campos de beneficiario/empresa
// van null si el rol no aplica. El rut es solo de lectura (no editable).
public record ProfileResponseDto(
        Long id,
        String name,
        String apellido,
        String email,
        String phone,
        Long regionId,
        String regionNombre,
        Long comunaId,
        String comunaNombre,
        String role,
        String rut,
        String direccionEntrega,
        String observaciones,
        String razonSocial,
        String giro,
        String direccionLegal
) {}
