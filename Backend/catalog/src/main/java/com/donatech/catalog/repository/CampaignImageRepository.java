package com.donatech.catalog.repository;

import com.donatech.catalog.model.CampaignImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignImageRepository extends JpaRepository<CampaignImage, Long> {
    List<CampaignImage> findByCampaignIdOrderByOrden(Long campaignId);
    long countByCampaignId(Long campaignId);
}
