package com.donatech.shipping.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderShippedEvent(Long orderId, String recipientEmail, String trackingNumber) {}
