package com.donatech.order.event;

// Evento dirigido al beneficiario cuando se sube evidencia de entrega
// (PENDIENTE_CONFIRMACION): se le pide confirmar la recepción o reportar a soporte.
public record DeliveryConfirmRequestEvent(
        Long orderId,
        String beneficiaryEmail,
        String beneficiaryName,
        String kitNames,
        String confirmLink,
        String supportLink
) {}
