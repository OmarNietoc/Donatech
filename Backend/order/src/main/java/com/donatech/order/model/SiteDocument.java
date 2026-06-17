package com.donatech.order.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Documento legal editable por admin (Términos, Privacidad). Contenido en Markdown.
@Entity
@Table(name = "site_document")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteDocument {

    @Id
    @Column(length = 40)
    private String slug;   // TERMS | PRIVACY

    @Column(nullable = false)
    private String titulo;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
