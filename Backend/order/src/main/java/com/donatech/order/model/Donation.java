package com.donatech.order.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Donación = intención/pago del donante. Agrupa una o más {@link Order} (una por campaña/beneficiario).
 * El comprobante de transferencia y su validación viven aquí (a nivel donación, un solo pago).
 * Tabla `donation_payments` para no colisionar con la tabla `donations` (que es Order, la unidad de fulfillment).
 */
@Entity
@Table(name = "donation_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    private String donorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", nullable = false)
    @Builder.Default
    private PaymentStatus estadoPago = PaymentStatus.INGRESADA;

    @Column(name = "coupon_code")
    private String couponCode;

    @Column(name = "discount_applied")
    @Builder.Default
    private Integer discountApplied = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer total = 0;

    @Column(name = "transfer_proof_url", length = 500)
    private String transferProofUrl;

    @Column(name = "transfer_proof_uploaded_at")
    private LocalDateTime transferProofUploadedAt;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(name = "fecha_creacion", nullable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @OneToMany(mappedBy = "donation", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<Order> orders = new ArrayList<>();
}
