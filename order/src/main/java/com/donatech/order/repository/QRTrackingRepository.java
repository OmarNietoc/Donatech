package com.donatech.order.repository;

import com.donatech.order.model.QRTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QRTrackingRepository extends JpaRepository<QRTracking, Long> {
    List<QRTracking> findByDonationIdOrderByScannedAtDesc(Long donationId);
}
