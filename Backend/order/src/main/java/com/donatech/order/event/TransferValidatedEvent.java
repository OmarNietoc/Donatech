package com.donatech.order.event;

public record TransferValidatedEvent(
        Long donationId,
        boolean approved,
        String motivo
) {}
