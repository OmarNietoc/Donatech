package com.donatech.order.event;

import java.util.List;

/** Recibido de shipping al crear una ruta: asigna colaborador y mueve órdenes a ASIGNADA_ENVIO. */
public record RouteAssignedEvent(
        String routeId,
        String routeName,
        List<Long> orderIds,
        Long collaboratorId,
        String collaboratorNombre,
        String collaboratorEmail
) {}
