package com.donatech.order.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transfer_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferConfig {

    @Id
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String banco;

    @NotBlank
    @Column(name = "tipo_cuenta", nullable = false)
    private String tipoCuenta;

    @NotBlank
    @Column(name = "nro_cuenta", nullable = false)
    private String nroCuenta;

    @NotBlank
    @Column(nullable = false)
    private String rut;

    @NotBlank
    @Column(name = "nombre_beneficiario", nullable = false)
    private String nombreBeneficiario;

    @Column
    private String email;
}
