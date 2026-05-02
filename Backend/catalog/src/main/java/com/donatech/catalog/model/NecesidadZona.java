package com.donatech.catalog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "necesidades_zona")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NecesidadZona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El producto es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product producto;

    @NotNull
    @Column(name = "comuna_id", nullable = false)
    private Long comunaId;

    @NotNull
    @Min(0)
    @Column(name = "cantidad_necesaria", nullable = false)
    private Integer cantidadNecesaria;

    @Min(0)
    @Column(name = "cantidad_cubierta", nullable = false)
    @Builder.Default
    private Integer cantidadCubierta = 0;
}
