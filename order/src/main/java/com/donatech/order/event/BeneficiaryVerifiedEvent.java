package com.donatech.order.event;

import java.time.LocalDateTime;

public record BeneficiaryVerifiedEvent(
        Long beneficiaryId,
        Long userId,
        String rut,
        LocalDateTime verifiedAt
) {}
