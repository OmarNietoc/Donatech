package com.donatech.order.event;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDeliveredEvent(
        Long orderId,
        String userEmail,
        Long campaignId,
        List<DonationItemEvent> items,
        LocalDateTime deliveredAt
) {}
