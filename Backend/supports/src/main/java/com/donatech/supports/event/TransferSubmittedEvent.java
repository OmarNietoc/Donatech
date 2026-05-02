package com.donatech.supports.event;

import java.time.LocalDateTime;

public record TransferSubmittedEvent(
        Long orderId,
        String userEmail,
        LocalDateTime submittedAt
) {}
