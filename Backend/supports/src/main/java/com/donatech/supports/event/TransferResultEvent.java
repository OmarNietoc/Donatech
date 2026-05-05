package com.donatech.supports.event;

public record TransferResultEvent(
        Long orderId,
        boolean approved,
        String motivo,
        String recipientEmail
) {}
