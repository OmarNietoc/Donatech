package com.donatech.catalog.repository;

import com.donatech.catalog.model.NecesidadZona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NecesidadZonaRepository extends JpaRepository<NecesidadZona, Long> {
    List<NecesidadZona> findByComunaId(Long comunaId);
    List<NecesidadZona> findByProductoId(String productoId);
}
