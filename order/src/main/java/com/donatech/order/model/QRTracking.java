package com.donatech.order.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "qr_tracking")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QRTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "donation_id", nullable = false)
    private Long donationId;

    @NotNull
    @Column(name = "scanned_at", nullable = false)
    @Builder.Default
    private LocalDateTime scannedAt = LocalDateTime.now();

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "scanned_by_email", length = 255)
    private String scannedByEmail;

    @Column(name = "notes", length = 1000)
    private String notes;
}
