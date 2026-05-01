package com.donatech.catalog.service;

import com.donatech.catalog.controller.response.MessageResponse;
import com.donatech.catalog.dto.ProductDto;
import com.donatech.catalog.model.Category;
import com.donatech.catalog.model.Product;
import com.donatech.catalog.model.Unit;
import com.donatech.catalog.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private UnitService unitService;

    @InjectMocks
    private ProductService productService;

    @Test
    void getProducts_WithCategoryAndUnit_ShouldDelegateToCombinedQuery() {
        Page<Product> expected = new PageImpl<>(List.of(Product.builder().id("SKU-1").build()));
        when(productRepository.findByCategoriaIdAndUnidId(eq(5L), eq(7L), any(Pageable.class)))
                .thenReturn(expected);

        Page<Product> result = productService.getProducts(0, 10, 5L, 7L);

        assertSame(expected, result);
        verify(productRepository, times(1))
                .findByCategoriaIdAndUnidId(eq(5L), eq(7L), any(Pageable.class));
    }

    @Test
    void createProduct_WithValidDto_ShouldPersistProduct() {
        ProductDto dto = buildProductDto();
        Category category = Category.builder().id(3L).name("Tech").build();
        Unit unit = Unit.builder().id(4L).name("Unidad").build();

        when(categoryService.getCategoryById(dto.getCategoriaId())).thenReturn(category);
        when(unitService.getUnitById(dto.getUnidadId())).thenReturn(unit);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> response = productService.createProduct(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody() instanceof MessageResponse);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        Product saved = productCaptor.getValue();
        assertEquals(dto.getId(), saved.getId());
        assertEquals(dto.getNombre(), saved.getNombre());
        assertEquals(dto.getStock(), saved.getStock());
        assertEquals(dto.getStockMinimo(), saved.getStockMinimo());
        assertEquals(dto.getPrecio(), saved.getPrecio());
        assertEquals(category, saved.getCategoria());
        assertEquals(unit, saved.getUnid());
    }

    @Test
    void updateProduct_WithValidDto_ShouldOverwriteEntity() {
        Product existing = Product.builder()
                .id("SKU-1")
                .nombre("Tablet")
                .descripcion("Old description with more than twenty chars")
                .precio(99)
                .stock(15)
                .stockMinimo(5)
                .activo(1)
                .categoria(Category.builder().id(1L).name("Old").build())
                .unid(Unit.builder().id(1L).name("Old unit").build())
                .imagen(null)
                .build();

        ProductDto dto = buildProductDto();
        Category newCategory = Category.builder().id(5L).name("New").build();
        Unit newUnit = Unit.builder().id(6L).name("Caja").build();

        when(productRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(categoryService.getCategoryById(dto.getCategoriaId())).thenReturn(newCategory);
        when(unitService.getUnitById(dto.getUnidadId())).thenReturn(newUnit);

        ResponseEntity<MessageResponse> response = productService.updateProduct(existing.getId(), dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto.getNombre(), existing.getNombre());
        assertEquals(dto.getPrecio(), existing.getPrecio());
        assertEquals(dto.getStock(), existing.getStock());
        assertEquals(dto.getStockMinimo(), existing.getStockMinimo());
        assertEquals(dto.getActivo(), existing.getActivo());
        assertEquals(newCategory, existing.getCategoria());
        assertEquals(newUnit, existing.getUnid());
        verify(productRepository).save(existing);
    }

    @Test
    void deleteProduct_ShouldRemoveEntity() {
        Product product = Product.builder()
                .id("SKU-1")
                .nombre("Laptop")
                .descripcion("Laptop description long enough")
                .precio(150)
                .stock(5)
                .stockMinimo(2)
                .activo(1)
                .categoria(Category.builder().id(1L).name("Tech").build())
                .unid(Unit.builder().id(1L).name("Unidad").build())
                .build();

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        ResponseEntity<MessageResponse> response = productService.deleteProduct(product.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productRepository).delete(product);
    }

    private ProductDto buildProductDto() {
        ProductDto dto = new ProductDto();
        dto.setId("SKU-1");
        dto.setNombre("Laptop Gamer");
        dto.setDescripcion("Laptop de 16 pulgadas con buena tarjeta grafica");
        dto.setCategoriaId(3L);
        dto.setUnidadId(4L);
        dto.setPrecio(1499);
        dto.setStock(20);
        dto.setStockMinimo(5);
        dto.setActivo(1);
        dto.setImagen(new byte[]{1, 2, 3});
        return dto;
    }
}
