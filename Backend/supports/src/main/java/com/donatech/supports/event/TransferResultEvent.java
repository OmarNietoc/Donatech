package com.donatech.supports.event;

public record TransferResultEvent(
        Long donationId,
        boolean approved,
        String motivo,
        String recipientEmail
) {}
