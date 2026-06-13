package com.donatech.order.event;

import java.time.LocalDateTime;
import java.util.List;

public record DonationConfirmedEvent(
        Long donationId,
        String donorEmail,
        Long campaignId,
        List<DonationItemEvent> items,
        LocalDateTime confirmedAt
) {}
