package com.donatech.notification.event;

import java.time.LocalDateTime;

public record DeliverySubmittedEvent(
        Long orderId,
        String userEmail,
        LocalDateTime submittedAt
) {}
