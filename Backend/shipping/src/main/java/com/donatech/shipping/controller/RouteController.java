package com.donatech.shipping.controller;

import com.donatech.shipping.dto.MessageResponse;
import com.donatech.shipping.dto.RouteCreationRequestDTO;
import com.donatech.shipping.dto.RouteDTO;
import com.donatech.shipping.dto.RouteReorderRequestDTO;
import com.donatech.shipping.enums.RouteStatus;
import com.donatech.shipping.mapper.RouteMapper;
import com.donatech.shipping.model.Route;
import com.donatech.shipping.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Rutas", description = "Creación y gestión de rutas de entrega con optimización OSRM")
@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;
    private final RouteMapper routeMapper;

    @Operation(summary = "Listar rutas", description = "Obtiene rutas con filtros opcionales por empresa y estado")
    @GetMapping
    public ResponseEntity<MessageResponse<List<RouteDTO>>> getAllRoutes(
            @RequestParam(required = false) String companyId,
            @RequestParam(required = false) RouteStatus status) {
        List<RouteDTO> routes = routeService.getAllRoutes(companyId, status).stream()
                .map(routeMapper::toDto)
                .collect(Collectors.toList());
        MessageResponse<List<RouteDTO>> response = MessageResponse.<List<RouteDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Listado de rutas obtenido exitosamente")
                .data(routes)
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Crear ruta", description = "Crea ruta de entrega. Con optimizeRoute=true usa OSRM para optimizar el orden de paradas")
    @PostMapping
    public ResponseEntity<MessageResponse<RouteDTO>> createRoute(@RequestBody RouteCreationRequestDTO request) {
        Route createdRoute = routeService.createRoute(
                request.getCompanyId(),
                request.getCarrierId(),
                request.getOriginAddress(),
                request.getShipmentIds(),
                request.isOptimizeRoute()
        );

        MessageResponse<RouteDTO> response = MessageResponse.<RouteDTO>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Ruta creada exitosamente.")
                .data(routeMapper.toDto(createdRoute))
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Obtener ruta por ID")
    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse<RouteDTO>> getRouteById(@PathVariable String id) {
        Route route = routeService.getRouteById(id);
        
        MessageResponse<RouteDTO> response = MessageResponse.<RouteDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Ruta obtenida exitosamente")
                .data(routeMapper.toDto(route))
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Actualizar estado de ruta", description = "Actualizar estado. IN_PROGRESS cambia todos los envíos a DISPATCHED automáticamente")
    @PatchMapping("/{id}/status")
    public ResponseEntity<MessageResponse<RouteDTO>> updateRouteStatus(@PathVariable String id, @RequestBody RouteStatus status) {
        Route updated = routeService.updateRouteStatus(id, status);
        MessageResponse<RouteDTO> response = MessageResponse.<RouteDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Estado de ruta actualizado exitosamente")
                .data(routeMapper.toDto(updated))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Reordenar envíos de una ruta",
            description = "Actualiza el orden de entrega de los envíos en una ruta PLANNED. " +
                    "Enviar la lista completa de shipmentIds en el nuevo orden deseado (drag & drop). " +
                    "Guarda el resultado con source='reordered' en optimizedPathJson.")
    @PatchMapping("/{id}/reorder")
    public ResponseEntity<MessageResponse<RouteDTO>> reorderShipments(
            @PathVariable String id,
            @Valid @RequestBody RouteReorderRequestDTO request) {
        Route updated = routeService.reorderShipments(id, request.getShipmentIds());
        MessageResponse<RouteDTO> response = MessageResponse.<RouteDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Orden de envíos actualizado exitosamente")
                .data(routeMapper.toDto(updated))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancelar ruta", description = "Soft delete: cancela la ruta y libera todos los envíos asignados a estado PENDING")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse<Void>> deleteRoute(@PathVariable String id) {
        routeService.deleteRoute(id);
        MessageResponse<Void> response = MessageResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Ruta cancelada exitosamente y envíos liberados")
                .data(null)
                .build();
        return ResponseEntity.ok(response);
    }
}

