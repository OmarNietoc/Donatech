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
public class Order {

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

    // Cargo de logística (costo por kit de la campaña × cantidad de kits), incluido en finalPrice.
    @Column(name = "logistics_cost")
    @Builder.Default
    private Integer logisticsCost = 0;

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

    @Column(name = "campaign_id")
    private Long campaignId;

    @Column(name = "transfer_proof_url", length = 500)
    private String transferProofUrl;

    @Column(name = "transfer_proof_uploaded_at")
    private LocalDateTime transferProofUploadedAt;

    @Column(name = "transportista_nombre", length = 150)
    private String transportistaNombre;

    @Column(name = "transportista_contacto", length = 100)
    private String transportistaContacto;

    @Column(name = "courier_assigned_at")
    private LocalDateTime courierAssignedAt;

    @Column(name = "delivery_photo_url", length = 500)
    private String deliveryPhotoUrl;

    @Column(name = "delivery_document_url", length = 500)
    private String deliveryDocumentUrl;

    @Column(name = "delivery_confirmed_at")
    private LocalDateTime deliveryConfirmedAt;

    @Column(name = "delivery_confirmed_by")
    private Long deliveryConfirmedBy;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    // ─── Mensaje de agradecimiento del beneficiario al donante ───
    @Column(name = "thank_you_message", length = 600)
    private String thankYouMessage;

    @Column(name = "thank_you_sent_at")
    private LocalDateTime thankYouSentAt;

    @ElementCollection
    @CollectionTable(name = "donation_thank_you_images", joinColumns = @JoinColumn(name = "donation_id"))
    @Column(name = "image_url", length = 500)
    @Builder.Default
    private List<String> thankYouImageUrls = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
}
