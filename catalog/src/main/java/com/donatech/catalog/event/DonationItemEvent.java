package com.donatech.catalog.event;

public record DonationItemEvent(
        Long kitId,
        Integer quantity
) {}
