package com.donatech.order.client;

import com.donatech.order.dto.CampaignSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalog-service", contextId = "campaignClient", path = "/api/campaigns", fallback = CampaignClientFallback.class)
public interface CampaignClient {

    @GetMapping("/{id}")
    CampaignSummaryDto getCampaignById(@PathVariable("id") Long id);
}
