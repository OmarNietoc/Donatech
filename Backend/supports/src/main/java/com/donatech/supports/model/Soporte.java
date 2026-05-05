package com.donatech.supports.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "soportes")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Soporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La descripción no puede estar vacía")
    @Column(nullable = false, length = 2000)
    private String descripcion;

    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoSoporte estado = EstadoSoporte.PENDIENTE;

    @NotNull(message = "El usuarioId no puede estar vacío")
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @NotNull(message = "La prioridad es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PrioridadSoporte prioridad;

    @NotNull(message = "El tipo es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoSoporte tipo;

    @Column(length = 255)
    private String titulo;

    @Column(name = "donation_id")
    private Long donationId;

    @Column(name = "campaign_id")
    private Long campaignId;

    @Column(name = "asignado_a")
    private Long asignadoA;

    @Column(name = "respuesta", length = 2000)
    private String respuesta;

    @Column(name = "fecha_creacion", nullable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Column(name = "recipient_email", length = 255)
    private String recipientEmail;
}
