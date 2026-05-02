package com.donatech.supports.event;

import java.time.LocalDateTime;

public record CampaignCreatedEvent(
        Long campaignId,
        Long beneficiaryId,
        String titulo,
        String motivo,
        LocalDateTime createdAt
) {}
