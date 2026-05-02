package com.donatech.order.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
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

    @NotNull(message = "El ID del kit es obligatorio")
    @Column(name = "kit_id", nullable = false)
    private Long kitId;

    @Column(name = "kit_name_snapshot", length = 120)
    private String kitNameSnapshot;

    @NotNull(message = "El precio unitario es obligatorio")
    @Column(name = "unit_price")
    private Integer unitPrice;

    @NotNull(message = "La cantidad no puede ser nula")
    @Positive(message = "La cantidad debe ser mayor que 0")
    private Integer quantity;

    @NotNull(message = "El subtotal es obligatorio")
    @Column(name = "subtotal")
    private Integer subtotal;
}
