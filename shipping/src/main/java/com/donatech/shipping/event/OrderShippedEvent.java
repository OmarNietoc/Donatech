package com.donatech.shipping.event;

public record OrderShippedEvent(Long orderId, String recipientEmail, String trackingNumber) {}
