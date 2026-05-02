package com.donatech.shipping.model;

import jakarta.persistence.*;
import lombok.*;

import com.donatech.shipping.enums.RouteStatus;

import java.time.LocalDate;
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

    @Column(name = "company_id", nullable = false)
    private String companyId;

    @Column(name = "carrier_id")
    private String carrierId;

    @Column(name = "route_date", nullable = false)
    private LocalDate routeDate;

    @Column(name = "origin_address")
    private String originAddress;

    @Column(name = "optimized_path_json", columnDefinition = "TEXT")
    private String optimizedPathJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RouteStatus status;

    @OneToMany(mappedBy = "route", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private List<Shipment> shipments = new ArrayList<>();
}

