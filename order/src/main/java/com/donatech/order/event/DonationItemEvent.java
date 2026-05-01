package com.donatech.order.event;

public record DonationItemEvent(
        String productId,
        Integer quantity
) {}
