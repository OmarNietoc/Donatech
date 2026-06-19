package com.donatech.catalog.repository;

import com.donatech.catalog.model.KitItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KitItemRepository extends JpaRepository<KitItem, Long> {

    boolean existsByProductId(String productId);

    List<KitItem> findByProductId(String productId);
}
