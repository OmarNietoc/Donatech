package com.donatech.notification.event;

import java.time.LocalDateTime;

public record OrderDeliveredEvent(
        Long orderId,
        String userEmail,
        LocalDateTime deliveredAt
) {}
