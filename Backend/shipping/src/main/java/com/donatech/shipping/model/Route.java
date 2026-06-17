package com.donatech.shipping.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.donatech.shipping.enums.RouteStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "routes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "company_id", nullable = false)
    private String companyId;

    @Column(name = "carrier_id")
    private String carrierId;

    // Colaborador (ROLE_VOLUNTARIO) asignado a la ruta
    @Column(name = "collaborator_id")
    private Long collaboratorId;

    @Column(name = "collaborator_nombre")
    private String collaboratorNombre;

    @Column(name = "collaborator_email")
    private String collaboratorEmail;

    @Column(name = "route_date", nullable = false)
    private LocalDate routeDate;

    @Column(name = "origin_address")
    private String originAddress;

    @Column(name = "optimized_path_json", columnDefinition = "TEXT")
    private String optimizedPathJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RouteStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "route", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private List<Shipment> shipments = new ArrayList<>();
}

