package com.donatech.order.event;

public record OrderReadyForShippingEvent(Long orderId, String userEmail, Long beneficiaryId, Long zonaCatastrofeId) {}
