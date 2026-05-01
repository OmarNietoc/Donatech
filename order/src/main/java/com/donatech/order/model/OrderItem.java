package com.donatech.order.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;

    @NotBlank(message = "El identificador del producto es obligatorio")
    @Column(name = "product_id", nullable = false, length = 50)
    private String productId;

    // Datos cacheados al añadir al carrito
    private String productName;
    @Column(name = "product_description", length = 1000)
    private String productDescription;
    @NotNull(message = "El precio unitario es obligatorio")
    private Integer unitPrice;

    @Lob
    @Column(name = "product_image")
    private String productImage;

    @NotNull(message = "La cantidad no puede ser nula")
    @Positive(message = "La cantidad debe ser mayor que 0")
    private Integer quantity;

    @NotNull(message = "El subtotal es obligatorio")
    @Column(name = "subtotal")
    private Integer subtotal;

    @Column(name = "kit_id")
    private Long kitId;

    @Column(name = "kit_name", length = 120)
    private String kitName;
}
