package com.donatech.order.client;

import com.donatech.order.dto.CampaignSummaryDto;
import org.springframework.stereotype.Component;

@Component
public class CampaignClientFallback implements CampaignClient {

    @Override
    public CampaignSummaryDto getCampaignById(Long id) {
        return null;
    }
}
