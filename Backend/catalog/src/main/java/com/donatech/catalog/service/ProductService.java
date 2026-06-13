package com.donatech.catalog.service;

import com.donatech.catalog.controller.response.MessageResponse;
import com.donatech.catalog.dto.ProductDto;
import com.donatech.catalog.dto.response.ProductResponseDto;
import com.donatech.catalog.exception.ResourceNotFoundException;
import com.donatech.catalog.model.Category;
import com.donatech.catalog.model.Prioridad;
import com.donatech.catalog.model.Product;
import com.donatech.catalog.model.Unit;
import com.donatech.catalog.repository.ProductRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final UnitService unitService;
    private final ImageStorageService imageStorageService;

    public Page<ProductResponseDto> getProducts(Integer page, Integer size, Long categoryId, Long unitId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Product> products;
        if (categoryId != null && unitId != null) {
            products = productRepository.findByCategoriaIdAndUnidId(categoryId, unitId, pageable);
        } else if (categoryId != null) {
            products = productRepository.findByCategoriaId(categoryId, pageable);
        } else if (unitId != null) {
            products = productRepository.findByUnidId(unitId, pageable);
        } else {
            products = productRepository.findAll(pageable);
        }
        return products.map(this::toDto);
    }

    public ProductResponseDto getProductById(String id) {
        return toDto(productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + id)));
    }

    private ProductResponseDto toDto(Product p) {
        return ProductResponseDto.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .precio(p.getPrecio())
                .stock(p.getStock())
                .stockMinimo(p.getStockMinimo())
                .activo(p.getActivo())
                .prioridad(p.getPrioridad())
                .categoriaId(p.getCategoria() != null ? p.getCategoria().getId() : null)
                .categoriaNombre(p.getCategoria() != null ? p.getCategoria().getName() : null)
                .unidadId(p.getUnid() != null ? p.getUnid().getId() : null)
                .unidadNombre(p.getUnid() != null ? p.getUnid().getName() : null)
                .hasImage(p.getImagenUrl() != null)
                .build();
    }

    public ResponseEntity<?> createProduct(@Valid ProductDto dto) {
        if (productRepository.existsById(dto.getId())) {
            throw new com.donatech.catalog.exception.ConflictException(
                    "Ya existe un producto con el ID '" + dto.getId() + "'. Usa un identificador diferente.");
        }
        Category category = categoryService.getCategoryById(dto.getCategoriaId());
        Unit unit = unitService.getUnitById(dto.getUnidadId());

        Product product = Product.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .precio(dto.getPrecio())
                .stock(dto.getStock())
                .stockMinimo(resolveStockMinimo(dto.getStockMinimo()))
                .activo(resolveActivo(dto.getActivo()))
                .categoria(category)
                .unid(unit)
                .prioridad(dto.getPrioridad())
                .build();

        productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Producto creado exitosamente."));
    }

    public ResponseEntity<MessageResponse> updateProduct(String id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + id));
        Category category = categoryService.getCategoryById(dto.getCategoriaId());
        Unit unit = unitService.getUnitById(dto.getUnidadId());

        product.setNombre(dto.getNombre());
        product.setDescripcion(dto.getDescripcion());
        product.setPrecio(dto.getPrecio());
        product.setStock(dto.getStock());
        product.setStockMinimo(resolveStockMinimo(dto.getStockMinimo()));
        product.setActivo(resolveActivo(dto.getActivo()));
        product.setCategoria(category);
        product.setUnid(unit);
        product.setPrioridad(dto.getPrioridad());

        productRepository.save(product);
        return ResponseEntity.ok(new MessageResponse("Producto actualizado correctamente."));
    }

    @Transactional
    public ResponseEntity<MessageResponse> uploadImage(String id, MultipartFile file) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + id));
        try {
            String path = imageStorageService.store("products", id, file);
            product.setImagenUrl(path);
            productRepository.save(product);
            return ResponseEntity.ok(new MessageResponse("Imagen actualizada."));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al guardar imagen: " + e.getMessage()));
        }
    }

    public byte[] getImage(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + id));
        if (product.getImagenUrl() == null) return null;
        try {
            return imageStorageService.load(product.getImagenUrl());
        } catch (IOException e) {
            return null;
        }
    }

    public String getImageContentType(String id) {
        return productRepository.findById(id)
                .map(p -> p.getImagenUrl() != null ? imageStorageService.detectContentType(p.getImagenUrl()) : "image/jpeg")
                .orElse("image/jpeg");
    }

    @Transactional
    public Product deductStock(String productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + productId));
        int newStock = Math.max(0, product.getStock() - quantity);
        product.setStock(newStock);
        return productRepository.save(product);
    }

    @Transactional
    public Product restoreStock(String productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + productId));
        product.setStock(product.getStock() + quantity);
        return productRepository.save(product);
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    public List<Product> getProductsByPriority(Prioridad prioridad) {
        return productRepository.findByPrioridad(prioridad);
    }

    public ResponseEntity<MessageResponse> deleteProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + id));
        productRepository.delete(product);
        return ResponseEntity.ok(new MessageResponse("Producto eliminado correctamente."));
    }

    private Integer resolveStockMinimo(Integer value) {
        return value != null ? value : Product.DEFAULT_STOCK_MINIMO;
    }

    private Integer resolveActivo(Integer value) {
        return value != null ? value : Product.DEFAULT_ACTIVO;
    }
}
