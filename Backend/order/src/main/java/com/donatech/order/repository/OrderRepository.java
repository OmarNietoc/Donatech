package com.donatech.order.repository;

import com.donatech.order.model.DonationStatus;
import com.donatech.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findFirstByUserEmailAndEstadoOrderByOrderDateDesc(String userEmail, DonationStatus estado);

    List<Order> findByUserEmail(String userEmail);

    List<Order> findByBeneficiaryId(Long beneficiaryId);

    List<Order> findByZonaCatastrofeId(Long zonaCatastrofeId);
}
