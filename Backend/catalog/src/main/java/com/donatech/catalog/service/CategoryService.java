package com.donatech.catalog.service;

import com.donatech.catalog.exception.ConflictException;
import com.donatech.catalog.exception.ResourceNotFoundException;
import com.donatech.catalog.model.Category;
import com.donatech.catalog.repository.CategoryRepository;
import com.donatech.catalog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category no encontrada: " + id));
    }

    public void createCategory(Category category) {
        if (categoryRepository.existsByNameIgnoreCase(category.getName())) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre.");
        }
        categoryRepository.save(category);
    }

    public void updateCategory(Long id, Category updatedCategory) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category no encontrada: " + id));

        boolean nombreEnUso = categoryRepository.existsByNameIgnoreCaseAndIdNot(updatedCategory.getName(), id);
        if (nombreEnUso) {
            throw new ConflictException("Ya existe otra categoría con ese nombre.");
        }

        existing.setName(updatedCategory.getName());
        categoryRepository.save(existing);
    }

    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        if (productRepository.existsProductsByCategoriaId(id)) {
            throw new IllegalStateException("No se puede eliminar la categoría porque hay productos asociados a esta.");
        }

        categoryRepository.deleteById(id);
    }

}
