package com.donatech.notification.event;

import java.time.LocalDateTime;

public record TransferSubmittedEvent(
        Long orderId,
        String userEmail,
        LocalDateTime submittedAt
) {}
