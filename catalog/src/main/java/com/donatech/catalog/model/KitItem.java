package com.donatech.catalog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "kit_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KitItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kit_id", nullable = false)
    private Kit kit;

    @NotBlank
    @Column(name = "product_id", nullable = false)
    private String productId;

    @NotNull
    @Min(1)
    @Column(name = "cantidad_requerida", nullable = false)
    private Integer cantidadRequerida;
}
