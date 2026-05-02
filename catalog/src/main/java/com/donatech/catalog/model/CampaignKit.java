package com.donatech.catalog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "campaign_kits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignKit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    @JsonIgnore
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kit_id", nullable = false)
    private Kit kit;

    @NotNull
    @Min(1)
    @Column(name = "cantidad_necesaria", nullable = false)
    private Integer cantidadNecesaria;

    @Column(name = "cantidad_fulfilled", nullable = false)
    @Builder.Default
    private Integer cantidadFulfilled = 0;
}
