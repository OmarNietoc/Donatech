package com.donatech.order.event;

public record OrderShippedEvent(
        Long orderId,
        String recipientEmail,
        String trackingInfo
) {}
