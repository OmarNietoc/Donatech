package com.donatech.notification.event;

public record TransferResultEvent(
        Long donationId,
        boolean approved,
        String motivo,
        String recipientEmail
) {}
