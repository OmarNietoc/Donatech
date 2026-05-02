package com.donatech.order.event;

public record TransferValidatedEvent(
        Long orderId,
        boolean approved,
        String motivo
) {}
