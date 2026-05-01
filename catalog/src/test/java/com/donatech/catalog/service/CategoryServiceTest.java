package com.donatech.catalog.service;

import com.donatech.catalog.model.Category;
import com.donatech.catalog.repository.CategoryRepository;
import com.donatech.catalog.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createCategory_WhenNameIsAvailable_ShouldPersistCategory() {
        Category category = new Category();
        category.setName("Electrónica");
        when(categoryRepository.existsByNameIgnoreCase(category.getName())).thenReturn(false);

        categoryService.createCategory(category);

        verify(categoryRepository).save(category);
    }

    @Test
    void updateCategory_WithUniqueName_ShouldUpdateEntity() {
        Long id = 9L;
        Category existing = Category.builder().id(id).name("Cocina").build();
        Category updated = Category.builder().name("Hogar").build();

        when(categoryRepository.findById(id)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot(updated.getName(), id)).thenReturn(false);

        categoryService.updateCategory(id, updated);

        assertEquals("Hogar", existing.getName());
        verify(categoryRepository).save(existing);
    }

    @Test
    void deleteCategory_WhenNoProductsDependOnIt_ShouldDelete() {
        Long id = 3L;
        Category category = Category.builder().id(id).name("Oficina").build();

        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        when(productRepository.existsProductsByCategoriaId(id)).thenReturn(false);

        categoryService.deleteCategory(id);

        verify(categoryRepository).deleteById(id);
    }
}
