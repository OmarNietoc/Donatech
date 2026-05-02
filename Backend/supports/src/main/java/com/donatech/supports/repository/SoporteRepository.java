package com.donatech.supports.repository;

import com.donatech.supports.model.EstadoSoporte;
import com.donatech.supports.model.Soporte;
import com.donatech.supports.model.TipoSoporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SoporteRepository extends JpaRepository<Soporte, Long> {
    List<Soporte> findByEstado(EstadoSoporte estado);
    List<Soporte> findByTipo(TipoSoporte tipo);
    List<Soporte> findByUsuarioId(Long usuarioId);
    List<Soporte> findByAsignadoA(Long voluntarioId);
    List<Soporte> findByDonationId(Long donationId);
}
