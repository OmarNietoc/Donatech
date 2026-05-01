package com.donatech.catalog.repository;

import com.donatech.catalog.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    boolean existsProductsByCategoriaId(Long categoryId);

    boolean existsProductsByUnidId(Long unitId);

    Page<Product> findByCategoriaId(Long categoryId, Pageable pageable);

    Page<Product> findByUnidId(Long unitId, Pageable pageable);

    Page<Product> findByCategoriaIdAndUnidId(Long categoryId, Long unitId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.stock <= p.stockMinimo")
    List<Product> findLowStockProducts();

    List<Product> findByPrioridad(com.donatech.catalog.model.Prioridad prioridad);
}
