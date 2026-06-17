package com.donatech.order.repository;

import com.donatech.order.model.CertificateConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificateConfigRepository extends JpaRepository<CertificateConfig, Long> {
}
