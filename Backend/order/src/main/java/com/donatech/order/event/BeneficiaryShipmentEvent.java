package com.donatech.order.event;

public record BeneficiaryShipmentEvent(
        Long orderId,
        String beneficiaryEmail,
        String beneficiaryName,
        String kitNames,
        String direccion,
        String telefono,
        String supportLink,
        String fechaEstimada
) {}
