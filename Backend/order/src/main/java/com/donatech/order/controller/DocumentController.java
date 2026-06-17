package com.donatech.order.controller;

import com.donatech.order.model.SiteDocument;
import com.donatech.order.repository.SiteDocumentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/config/documents")
@RequiredArgsConstructor
@Tag(name = "Documentos legales", description = "Términos y Condiciones / Política de Privacidad (editables por admin)")
public class DocumentController {

    private final SiteDocumentRepository repository;

    public record DocumentBody(String titulo, String contenido) {}

    @Operation(summary = "Obtener documento legal por slug (público)")
    @GetMapping("/{slug}")
    public ResponseEntity<SiteDocument> get(@PathVariable String slug) {
        return repository.findById(slug.toUpperCase())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Actualizar documento legal (solo ADMIN)")
    @PutMapping("/{slug}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SiteDocument> save(@PathVariable String slug, @RequestBody DocumentBody body) {
        String key = slug.toUpperCase();
        SiteDocument doc = repository.findById(key).orElseGet(SiteDocument::new);
        doc.setSlug(key);
        if (body.titulo() != null) doc.setTitulo(body.titulo());
        doc.setContenido(body.contenido());
        doc.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(repository.save(doc));
    }
}
