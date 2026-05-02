package com.donatech.shipping.controller;

import com.donatech.shipping.dto.MessageResponse;
import com.donatech.shipping.dto.RouteCreationRequestDTO;
import com.donatech.shipping.dto.RouteDTO;
import com.donatech.shipping.enums.RouteStatus;
import com.donatech.shipping.mapper.RouteMapper;
import com.donatech.shipping.model.Route;
import com.donatech.shipping.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;
    private final RouteMapper routeMapper;

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

    @PostMapping
    public ResponseEntity<MessageResponse<RouteDTO>> createRoute(@RequestBody RouteCreationRequestDTO request) {
        Route createdRoute = routeService.createRoute(
                request.getCompanyId(),
                request.getCarrierId(),
                request.getOriginAddress(),
                request.getShipmentIds()
        );

        MessageResponse<RouteDTO> response = MessageResponse.<RouteDTO>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Ruta creada exitosamente.")
                .data(routeMapper.toDto(createdRoute))
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

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

