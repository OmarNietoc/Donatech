package com.donatech.order.event;

public record DonationItemEvent(
        Long kitId,
        Integer quantity
) {}
