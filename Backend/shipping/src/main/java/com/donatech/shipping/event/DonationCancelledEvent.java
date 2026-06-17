package com.donatech.shipping.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Recibido de order ms (donation.cancelled): la donación/orden fue cancelada. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DonationCancelledEvent(Long donationId) {}
