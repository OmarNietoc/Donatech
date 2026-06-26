package com.donatech.catalog.controller;

import com.donatech.catalog.controller.response.MessageResponse;
import com.donatech.catalog.dto.ProductDto;
import com.donatech.catalog.dto.response.ProductResponseDto;
import com.donatech.catalog.model.Prioridad;
import com.donatech.catalog.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Products", description = "Gestión de insumos y productos de catálogo")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Listar productos con paginación")
    @GetMapping
    public ResponseEntity<?> getProducts(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long unitId) {

        Page<ProductResponseDto> products = productService.getProducts(page, size, categoryId, unitId);
        List<ProductResponseDto> content = products.getContent();
        if (content.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(content);
    }

    @Operation(summary = "Obtener producto por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @Operation(summary = "Feed de productos activos con stock (consumo interno kit-ia)")
    @GetMapping("/active")
    public ResponseEntity<List<ProductResponseDto>> getActiveProducts() {
        return ResponseEntity.ok(productService.getActiveInStock());
    }

    @Operation(summary = "Imagen de producto")
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable String id) {
        byte[] bytes = productService.getImage(id);
        if (bytes == null || bytes.length == 0) return ResponseEntity.notFound().build();
        String ct = productService.getImageContentType(id);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(ct)).body(bytes);
    }

    @Operation(summary = "Subir imagen de producto")
    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> uploadImage(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {
        return productService.uploadImage(id, file);
    }

    @Operation(summary = "Productos con stock bajo")
    @GetMapping("/low-stock")
    public ResponseEntity<List<?>> getLowStockProducts() {
        return ResponseEntity.ok(productService.getLowStockProducts());
    }

    @Operation(summary = "Productos por prioridad")
    @GetMapping("/by-priority")
    public ResponseEntity<List<?>> getByPriority(@RequestParam Prioridad prioridad) {
        return ResponseEntity.ok(productService.getProductsByPriority(prioridad));
    }

    @Operation(summary = "Crear nuevo producto")
    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductDto dto) {
        return productService.createProduct(dto);
    }

    @Operation(summary = "Actualizar producto existente")
    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductDto productDto) {
        return productService.updateProduct(id, productDto);
    }

    @Operation(summary = "Eliminar producto")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteProduct(@PathVariable String id) {
        return productService.deleteProduct(id);
    }
}
