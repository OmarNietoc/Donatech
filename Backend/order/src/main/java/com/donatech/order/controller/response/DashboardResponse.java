package com.donatech.order.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {
    private long totalDonations;
    private long totalItems;
    private Map<String, Long> donationsByStatus;
    private Map<Long, Long> donationsByZone;
}
