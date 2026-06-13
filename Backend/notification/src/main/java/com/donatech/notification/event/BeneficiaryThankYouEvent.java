package com.donatech.notification.event;

public record BeneficiaryThankYouEvent(
        Long orderId,
        String donorEmail,
        String donorName,
        String beneficiaryName,
        String message,
        int imageCount
) {}
