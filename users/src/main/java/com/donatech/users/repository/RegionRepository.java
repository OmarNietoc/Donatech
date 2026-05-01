package com.donatech.users.repository;

import com.donatech.users.model.Comuna;
import com.donatech.users.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
    Region getRegionById(Long region);
}
