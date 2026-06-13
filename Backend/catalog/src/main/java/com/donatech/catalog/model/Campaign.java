package com.donatech.catalog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "campaigns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El ID del beneficiario es obligatorio")
    @Column(name = "beneficiary_id", nullable = false)
    private Long beneficiaryId;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200)
    @Column(nullable = false)
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 100)
    @Column(nullable = false)
    private String motivo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CampaignStatus estado = CampaignStatus.EN_VALIDACION;

    @Column(name = "region_id")
    private Long regionId;

    @Column(name = "comuna_id")
    private Long comunaId;

    @Column(name = "fecha_creacion", nullable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_activacion")
    private LocalDateTime fechaActivacion;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Size(max = 2000)
    @Column(length = 2000)
    private String observaciones;

    @Size(max = 2000)
    @Column(name = "motivo_rechazo", length = 2000)
    private String motivoRechazo;

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CampaignKit> kits = new ArrayList<>();

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CampaignImage> images = new ArrayList<>();
}
