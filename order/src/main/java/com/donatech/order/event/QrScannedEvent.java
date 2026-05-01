package com.donatech.order.event;

import java.time.LocalDateTime;

public record QrScannedEvent(
        String qrCode,
        Long donationId,
        String scannedByEmail,
        String location,
        String notes,
        LocalDateTime scannedAt
) {}
