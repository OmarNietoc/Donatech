package com.donatech.notification.event;

public record DeliveryConfirmRequestEvent(
        Long orderId,
        String beneficiaryEmail,
        String beneficiaryName,
        String kitNames,
        String confirmLink,
        String supportLink
) {}
