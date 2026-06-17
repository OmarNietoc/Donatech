package com.donatech.order.model;

/**
 * Estado de pago/validación de una Donation (la transferencia que cubre TODAS sus órdenes hijas).
 * El fulfillment de cada orden vive en {@link DonationStatus}.
 */
public enum PaymentStatus {
    INGRESADA,                    // Donación creada, sin comprobante
    EN_VALIDACION_TRANSFERENCIA,  // Comprobante adjuntado, esperando validación
    APROBADA,                     // Transferencia validada → órdenes a EN_PREPARACION
    RECHAZADA,                    // Transferencia rechazada → órdenes RECHAZADA
    CANCELADA                     // Donación cancelada antes de validar
}
