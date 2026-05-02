package com.donatech.users.event;

import java.time.LocalDateTime;

public record BeneficiaryVerifiedEvent(
        Long beneficiaryId,
        Long userId,
        String rut,
        LocalDateTime verifiedAt
) {}
