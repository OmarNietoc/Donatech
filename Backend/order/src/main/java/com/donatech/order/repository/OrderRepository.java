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

    List<Order> findByCampaignId(Long campaignId);

    List<Order> findByCampaignIdAndEstadoIn(Long campaignId, java.util.Collection<DonationStatus> estados);

    List<Order> findByCollaboratorIdAndEstadoIn(Long collaboratorId, java.util.Collection<DonationStatus> estados);

    List<Order> findByEstadoIn(java.util.Collection<DonationStatus> estados);

    // Backfill: órdenes legacy aún no agrupadas bajo una Donation padre.
    List<Order> findByDonationIsNull();
}
