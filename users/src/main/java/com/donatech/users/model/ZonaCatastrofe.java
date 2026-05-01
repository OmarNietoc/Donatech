package com.donatech.users.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "zonas_catastrofe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZonaCatastrofe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "region_id", nullable = false)
    @NotNull
    private Region region;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "comuna_id", nullable = false)
    @NotNull
    private Comuna comuna;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String nombreEvento;

    @NotNull
    @Column(nullable = false)
    private LocalDate fechaDeclaracion;

    private LocalDate fechaFin;

    @Column(nullable = false)
    private Boolean activa = true;
}
