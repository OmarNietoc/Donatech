package com.donatech.users.repository;

import com.donatech.users.model.ZonaCatastrofe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZonaCatastrofeRepository extends JpaRepository<ZonaCatastrofe, Long> {
    List<ZonaCatastrofe> findByActivaTrue();
    List<ZonaCatastrofe> findByRegionId(Long regionId);
    List<ZonaCatastrofe> findByComunaId(Long comunaId);
}
