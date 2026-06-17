package com.donatech.notification.event;

import java.time.LocalDateTime;

public record TransferSubmittedEvent(
        Long donationId,
        String userEmail,
        LocalDateTime submittedAt
) {}
