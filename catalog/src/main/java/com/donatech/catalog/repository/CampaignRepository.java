package com.donatech.catalog.repository;

import com.donatech.catalog.model.Campaign;
import com.donatech.catalog.model.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByEstado(CampaignStatus estado);
    List<Campaign> findByBeneficiaryId(Long beneficiaryId);
}
