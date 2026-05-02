package com.donatech.order.event;

import java.time.LocalDateTime;

public record TransferSubmittedEvent(
        Long orderId,
        String userEmail,
        LocalDateTime submittedAt
) {}
