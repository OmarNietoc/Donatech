package com.donatech.catalog.event;

public record DonationItemEvent(
        String productId,
        Integer quantity
) {}
