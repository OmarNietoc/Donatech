package com.donatech.order.repository;

import com.donatech.order.model.TrackingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackingHistoryRepository extends JpaRepository<TrackingHistory, Long> {
    List<TrackingHistory> findByOrder_IdOrderByFechaCambioAsc(Long orderId);
}
