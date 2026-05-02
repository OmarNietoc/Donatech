package com.donatech.order.model;

public enum DonationStatus {
    DRAFT,                       // carrito (pre-envío, selección de kits)
    INGRESADA,                   // comprobante de transferencia adjunto
    EN_VALIDACION_TRANSFERENCIA, // ticket automático creado en supports
    EN_PREPARACION,              // transferencia validada, preparando productos
    ASIGNADA_ENVIO,              // asignada a shipping ms
    EN_CAMINO,                   // en tránsito hacia beneficiario
    PENDIENTE_CONFIRMACION,      // transportista subió foto + documento firmado
    ENTREGADA,                   // confirmación final del validador
    CANCELADA,
    RECHAZADA                    // transferencia rechazada por soporte
}
