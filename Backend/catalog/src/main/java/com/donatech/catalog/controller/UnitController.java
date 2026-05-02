package com.donatech.catalog.controller;

import com.donatech.catalog.controller.response.MessageResponse;
import com.donatech.catalog.model.Unit;
import com.donatech.catalog.service.UnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products/units")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;

    @Operation(
            summary = "Listar unidades",
            description = "Devuelve una lista con todas las unidades registradas en el sistema."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unidades listadas exitosamente")
    })
    @GetMapping
    public ResponseEntity<List<Unit>> getAllUnits() {
        return ResponseEntity.ok(unitService.getAllUnits());
    }

    @Operation(
            summary = "Obtener unidad por ID",
            description = "Busca y devuelve una unidad específica a partir de su ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unidad encontrada"),
            @ApiResponse(responseCode = "404", description = "Unidad no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getUnitById(@PathVariable Long id) {
        Unit unit = unitService.getUnitById(id);
        return ResponseEntity.ok(unit);
    }

    @Operation(
            summary = "Crear nueva unidad",
            description = "Crea una nueva unidad validando que no exista otra con el mismo nombre."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Unidad creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Nombre de la unidad ya existente")
    })
    @PostMapping("/add")
    public ResponseEntity<MessageResponse> createUnit(@Valid @RequestBody Unit unit) {
        unitService.createUnit(unit);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("Unidad creada exitosamente."));
    }

    @Operation(
            summary = "Actualizar unidad",
            description = "Actualiza la información de una unidad existente por su ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unidad actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Nombre duplicado o datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Unidad no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> updateUnit(@PathVariable Long id, @Valid @RequestBody Unit updatedUnit) {
        unitService.updateUnit(id, updatedUnit);
        return ResponseEntity.ok(new MessageResponse("Unidad actualizada exitosamente."));
    }

    @Operation(
            summary = "Eliminar unidad",
            description = "Elimina una unidad por ID, solo si no tiene productos asociados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unidad eliminada exitosamente"),
            @ApiResponse(responseCode = "409", description = "No se puede eliminar la unidad porque hay productos asociados"),
            @ApiResponse(responseCode = "404", description = "Unidad no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteUnit(@PathVariable Long id) {
        unitService.deleteUnit(id);
        return ResponseEntity.ok(new MessageResponse("Unidad eliminada exitosamente."));
    }
}
