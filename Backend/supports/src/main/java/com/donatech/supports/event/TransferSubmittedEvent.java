package com.donatech.supports.event;

import java.time.LocalDateTime;

public record TransferSubmittedEvent(
        Long donationId,
        String userEmail,
        LocalDateTime submittedAt
) {}
