package com.donatech.notification.event;

public record TransferResultEvent(
        Long orderId,
        boolean approved,
        String motivo
) {}
