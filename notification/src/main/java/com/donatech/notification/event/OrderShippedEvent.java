package com.donatech.notification.event;

public record OrderShippedEvent(
        Long orderId,
        String recipientEmail,
        String trackingInfo
) {}
