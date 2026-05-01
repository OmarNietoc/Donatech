package com.donatech.catalog.controller;

import com.donatech.catalog.controller.response.MessageResponse;
import com.donatech.catalog.model.Category;
import com.donatech.catalog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Obtener todas las categorías", description = "Retorna una lista con todas las categorías disponibles.")
    @ApiResponse(responseCode = "200", description = "Lista de categorías retornada correctamente")
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Operation(summary = "Obtener categoría por ID", description = "Retorna una categoría según su ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @Operation(summary = "Crear nueva categoría", description = "Crea una nueva categoría a partir de los datos proporcionados.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Error de validación en los datos de entrada")
    })
    @PostMapping("/add")
    public ResponseEntity<MessageResponse> createCategory(@Valid @RequestBody Category category) {
        categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("Categoría creada exitosamente."));
    }

    @Operation(summary = "Actualizar categoría", description = "Actualiza los datos de una categoría existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría actualizada correctamente"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
            @ApiResponse(responseCode = "400", description = "Error de validación en los datos de entrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody Category category) {
        categoryService.updateCategory(id, category);
        return ResponseEntity.ok(new MessageResponse("Categoría actualizada exitosamente."));
    }

    @Operation(summary = "Eliminar categoría", description = "Elimina una categoría según su ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
            @ApiResponse(responseCode = "409", description = "No se puede eliminar la categoría porque hay cursos asociados")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(new MessageResponse("Categoría eliminada exitosamente."));
    }

}

