package com.donatech.shipping.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Recibido de order ms (order.delivered): la orden fue entregada al beneficiario. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderDeliveredEvent(Long orderId) {}
