package com.donatech.order.repository;

import com.donatech.order.model.SiteDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteDocumentRepository extends JpaRepository<SiteDocument, String> {
}
