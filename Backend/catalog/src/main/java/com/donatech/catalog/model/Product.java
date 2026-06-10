package com.donatech.catalog.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    public static final int DEFAULT_STOCK_MINIMO = 5;
    public static final int DEFAULT_ACTIVO = 1;

    @Id
    @Column(length = 20, nullable = false, unique = true)
    @NotBlank(message = "El id del producto es obligatorio")
    private String id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres")
    private String nombre;

    @NotBlank(message = "La descripcion es obligatoria")
    @Size(min = 20, max = 1000, message = "La descripcion debe tener entre 20 y 1000 caracteres")
    @Column(length = 1000)
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @PositiveOrZero(message = "El precio debe ser 0 o mayor")
    @Column(nullable = false)
    private Integer precio;

    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero(message = "El stock debe ser 0 o mayor")
    @Column(nullable = false)
    private Integer stock;

    @PositiveOrZero(message = "El stock minimo debe ser 0 o mayor")
    @Column(name = "stock_minimo", nullable = false)
    @Builder.Default
    private Integer stockMinimo = DEFAULT_STOCK_MINIMO;

    @Min(value = 0, message = "El estado activo solo puede ser 0 o 1")
    @Max(value = 1, message = "El estado activo solo puede ser 0 o 1")
    @Column(nullable = false)
    @Builder.Default
    private Integer activo = DEFAULT_ACTIVO;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    @NotNull(message = "La categoria es obligatoria")
    private Category categoria;

    @ManyToOne
    @JoinColumn(name = "unit_id", nullable = false)
    @NotNull(message = "La unidad es obligatoria")
    private Unit unid;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", length = 20)
    private Prioridad prioridad;
}
