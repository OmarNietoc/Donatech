package com.donatech.order.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Textos configurables (editables por admin) del certificado de donación. Singleton (id=1).
@Entity
@Table(name = "certificate_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateConfig {

    @Id
    private Long id;

    @Lob
    @Column(name = "clausula_legal", columnDefinition = "TEXT")
    private String clausulaLegal;

    @Column(name = "representante_nombre")
    private String representanteNombre;

    @Column(name = "representante_cargo")
    private String representanteCargo;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String pie;
}
