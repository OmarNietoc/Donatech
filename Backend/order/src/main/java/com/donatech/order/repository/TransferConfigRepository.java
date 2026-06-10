package com.donatech.order.repository;

import com.donatech.order.model.TransferConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferConfigRepository extends JpaRepository<TransferConfig, Long> {}
