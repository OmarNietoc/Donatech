package com.donatech.catalog.repository;

import com.donatech.catalog.model.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {
    Optional<Unit> findByNameIgnoreCase(String name);

    Optional<Unit> findByName(String unidadNombre);
}
