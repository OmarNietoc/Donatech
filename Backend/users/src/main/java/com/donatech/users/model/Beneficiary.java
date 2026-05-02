package com.donatech.users.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "beneficiaries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @NotNull
    private User user;

    @NotBlank
    @Column(unique = true, nullable = false, length = 12)
    private String rut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoVerificacion estadoVerificacion = EstadoVerificacion.PENDIENTE;

    @Column(length = 300)
    private String direccionEntrega;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_by_id")
    private User registeredBy;

    @Column(length = 500)
    private String motivoRegistro;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    private LocalDateTime fechaVerificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verificado_por_id")
    private User verificadoPor;

    @Column(length = 500)
    private String motivoRechazo;

    @Column(length = 1000)
    private String observaciones;
}
