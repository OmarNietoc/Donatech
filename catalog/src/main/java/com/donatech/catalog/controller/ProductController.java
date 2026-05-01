package com.donatech.catalog.controller;

import com.donatech.catalog.controller.response.MessageResponse;
import com.donatech.catalog.dto.ProductDto;
import com.donatech.catalog.model.Prioridad;
import com.donatech.catalog.model.Product;
import com.donatech.catalog.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(
            summary = "Listar productos con paginación y filtros opcionales",
            description = "Obtiene una lista paginada de productos, con opción de filtrar por categoría o unidad."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Productos listados exitosamente"),
            @ApiResponse(responseCode = "204", description = "No se encontraron productos")
    })
    @GetMapping
    public ResponseEntity<?> getProducts(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long unitId) {

        Page<Product> products = productService.getProducts(page, size, categoryId, unitId);
        List<Product> content = products.getContent();
        if (content.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(content);
    }

    @Operation(
            summary = "Obtener producto por ID",
            description = "Devuelve el detalle de un producto específico según su ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable String id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @Operation(summary = "Productos con stock bajo o igual al mínimo")
    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts() {
        return ResponseEntity.ok(productService.getLowStockProducts());
    }

    @Operation(summary = "Productos por prioridad")
    @ApiResponse(responseCode = "200", description = "Productos listados por prioridad")
    @GetMapping("/by-priority")
    public ResponseEntity<List<Product>> getByPriority(@RequestParam Prioridad prioridad) {
        return ResponseEntity.ok(productService.getProductsByPriority(prioridad));
    }

    @Operation(
            summary = "Crear nuevo producto",
            description = "Registra un nuevo producto a partir de un objeto ProductDto."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud")
    })
    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductDto dto) {
        return productService.createProduct(dto);
    }

    @Operation(
            summary = "Actualizar producto existente",
            description = "Actualiza los detalles de un producto a partir de su ID y un objeto ProductDto."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> updateProduct(@PathVariable String id, @Valid @RequestBody ProductDto productDto) {
        return productService.updateProduct(id, productDto);
    }

    @Operation(
            summary = "Eliminar producto",
            description = "Elimina un producto por su ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteProduct(@PathVariable String id) {
        return productService.deleteProduct(id);
    }
}
