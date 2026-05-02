package com.donatech.notification.event;

public record CampaignResultEvent(
        Long campaignId,
        boolean approved,
        String motivo
) {}
