package com.donatech.order.event;

import java.time.LocalDateTime;

public record DeliverySubmittedEvent(
        Long orderId,
        String userEmail,
        LocalDateTime submittedAt
) {}
