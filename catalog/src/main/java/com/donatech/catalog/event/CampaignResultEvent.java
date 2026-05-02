package com.donatech.catalog.event;

public record CampaignResultEvent(
        Long campaignId,
        boolean approved,
        String motivo   // motivo de rechazo si !approved, null si approved
) {}
