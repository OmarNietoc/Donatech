package com.donatech.notification.event;

import java.util.List;

public record RouteAssignedEvent(
        String routeId,
        List<Long> orderIds,
        Long collaboratorId,
        String collaboratorNombre,
        String collaboratorEmail
) {}
