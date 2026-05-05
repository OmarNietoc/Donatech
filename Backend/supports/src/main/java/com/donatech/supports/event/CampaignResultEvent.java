package com.donatech.supports.event;

public record CampaignResultEvent(
        Long campaignId,
        boolean approved,
        String motivo,
        String recipientEmail
) {}
