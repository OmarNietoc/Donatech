package com.donatech.shipping.event;

public record OrderReadyForShippingEvent(Long orderId, String userEmail, Long beneficiaryId, Long zonaCatastrofeId) {}
