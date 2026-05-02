package com.donatech.users.repository;

import com.donatech.users.model.Comuna;
import com.donatech.users.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComunaRepository extends JpaRepository<Comuna, Long> {
    List<Comuna> findByRegionId(Long regionId);

    Comuna getComunaById(Long comunaId);
}
