package com.donatech.users.repository;

import com.donatech.users.model.Beneficiary;
import com.donatech.users.model.EstadoVerificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    Optional<Beneficiary> findByRut(String rut);
    boolean existsByRut(String rut);
    boolean existsByUserId(Long userId);
    Optional<Beneficiary> findByUserId(Long userId);
    List<Beneficiary> findByEstadoVerificacion(EstadoVerificacion estado);
}
