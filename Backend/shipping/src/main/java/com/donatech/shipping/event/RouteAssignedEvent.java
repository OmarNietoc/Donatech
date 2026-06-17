package com.donatech.shipping.event;

import java.util.List;

/** Publicado al crear una ruta: notifica a order (cambiar estado) y notification (email colaborador). */
public record RouteAssignedEvent(
        String routeId,
        String routeName,
        List<Long> orderIds,
        Long collaboratorId,
        String collaboratorNombre,
        String collaboratorEmail
) {}
