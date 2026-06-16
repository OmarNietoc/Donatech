package com.donatech.catalog.repository;

import com.donatech.catalog.model.CampaignKit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignKitRepository extends JpaRepository<CampaignKit, Long> {
    List<CampaignKit> findByCampaignId(Long campaignId);
    Optional<CampaignKit> findByCampaignIdAndKitId(Long campaignId, Long kitId);
    List<CampaignKit> findByKitId(Long kitId);
    boolean existsByKitId(Long kitId);
}
