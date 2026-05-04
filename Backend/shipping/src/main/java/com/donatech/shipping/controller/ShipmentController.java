package com.donatech.shipping.controller;

import com.donatech.shipping.dto.MessageResponse;
import com.donatech.shipping.dto.ShipmentDTO;
import com.donatech.shipping.enums.DeliveryStatus;
import com.donatech.shipping.mapper.ShipmentMapper;
import com.donatech.shipping.model.Shipment;
import com.donatech.shipping.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Envíos", description = "Gestión de envíos y seguimiento de paquetes")
@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final ShipmentMapper shipmentMapper;

    @Operation(summary = "Listar envíos", description = "Obtiene todos los envíos, con filtro opcional por estado")
    @GetMapping
    public ResponseEntity<MessageResponse<List<ShipmentDTO>>> getAllShipments(
            @RequestParam(required = false) DeliveryStatus deliveryStatus) {
        List<ShipmentDTO> shipments = shipmentService.getAllShipments(deliveryStatus).stream()
                .map(shipmentMapper::toDto)
                .collect(Collectors.toList());
        MessageResponse<List<ShipmentDTO>> response = MessageResponse.<List<ShipmentDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Listado de envíos obtenido")
                .data(shipments)
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener envío por ID")
    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse<ShipmentDTO>> getShipmentById(@PathVariable String id) {
        Shipment shipment = shipmentService.getShipmentById(id);
        MessageResponse<ShipmentDTO> response = MessageResponse.<ShipmentDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Envío obtenido con éxito")
                .data(shipmentMapper.toDto(shipment))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener envío por número de seguimiento")
    @GetMapping("/tracking/{tracking_number}")
    public ResponseEntity<MessageResponse<ShipmentDTO>> getShipmentByTrackingNumber(@PathVariable("tracking_number") String trackingNumber) {
        Shipment shipment = shipmentService.getShipmentByTrackingNumber(trackingNumber);
        MessageResponse<ShipmentDTO> response = MessageResponse.<ShipmentDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Envío obtenido con éxito")
                .data(shipmentMapper.toDto(shipment))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Crear envío")
    @PostMapping
    public ResponseEntity<MessageResponse<ShipmentDTO>> createShipment(@RequestBody ShipmentDTO shipmentDto) {
        Shipment created = shipmentService.createShipment(shipmentMapper.toEntity(shipmentDto));
        MessageResponse<ShipmentDTO> response = MessageResponse.<ShipmentDTO>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Envío creado exitosamente")
                .data(shipmentMapper.toDto(created))
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar estado de envío")
    @PatchMapping("/{id}/status")
    public ResponseEntity<MessageResponse<ShipmentDTO>> updateShipmentStatus(@PathVariable String id, @RequestBody DeliveryStatus status) {
        Shipment updated = shipmentService.updateShipmentStatus(id, status);
        MessageResponse<ShipmentDTO> response = MessageResponse.<ShipmentDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Estado de envío actualizado exitosamente")
                .data(shipmentMapper.toDto(updated))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Eliminar envío")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse<Void>> deleteShipment(@PathVariable String id) {
        shipmentService.deleteShipment(id);
        MessageResponse<Void> response = MessageResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Envío eliminado exitosamente")
                .data(null)
                .build();
        return ResponseEntity.ok(response);
    }
}

