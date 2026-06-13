package com.donatech.catalog.event;

import java.time.LocalDateTime;
import java.util.List;

public record DonationCancelledEvent(
        Long donationId,
        String userEmail,
        Long campaignId,
        List<DonationItemEvent> items,
        LocalDateTime cancelledAt
) {}
