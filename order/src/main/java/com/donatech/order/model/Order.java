package com.donatech.order.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "donations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El email del usuario es obligatorio")
    @Email(message = "El email del usuario debe ser válido")
    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    private DonationStatus estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    @Nullable
    private Coupon coupon;

    @NotNull(message = "El precio final no puede ser nulo")
    private Integer finalPrice;

    @NotNull(message = "El descuento aplicado no puede ser nulo")
    private Integer discountApplied;

    @NotNull(message = "La fecha de Orden es obligatoria")
    private LocalDateTime orderDate;

    private String donorName;

    @Column(name = "is_anonymous")
    @Builder.Default
    private Boolean isAnonymous = false;

    @Column(name = "beneficiary_id")
    private Long beneficiaryId;

    @Column(name = "zona_catastrofe_id")
    private Long zonaCatastrofeId;

    @Column(name = "qr_code", length = 500)
    private String qrCode;

    @Column(name = "qr_generated_at")
    private LocalDateTime qrGeneratedAt;

    @Column(name = "tracking_notes", length = 1000)
    private String trackingNotes;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

}
