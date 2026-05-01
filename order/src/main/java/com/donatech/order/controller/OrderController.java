package com.donatech.order.controller;

import com.donatech.order.controller.response.MessageResponse;
import com.donatech.order.controller.response.OrderResponse;
import com.donatech.order.dto.OrderDto;
import com.donatech.order.model.DonationStatus;
import com.donatech.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.donatech.order.dto.AddItemToOrderRequest;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Listar órdenes", description = "Obtiene todas las órdenes registradas.")
    @ApiResponse(responseCode = "200", description = "Órdenes listadas correctamente")
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return orderService.getAllOrders();
    }

    @Operation(summary = "Obtener orden por ID", description = "Devuelve los detalles de una orden específica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orden encontrada"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return orderService.getOrderDtoById(id);
    }

    @Operation(summary = "Crear nueva orden", description = "Registra una nueva orden con sus ítems.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Orden creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderDto dto) {
        return orderService.createOrder(dto);
    }

    @Operation(summary = "Actualizar una orden", description = "Reemplaza los ítems y el cupón de una orden existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orden actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable Long id, @Valid @RequestBody OrderDto dto) {
        return orderService.updateOrder(id, dto);
    }

    // Obtener carrito activo por usuario
    @Operation(
            summary = "Obtener carrito activo por usuario",
            description = "Devuelve la orden en estado PENDING para el email indicado, incluyendo todos sus ítems."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrito encontrado"),
            @ApiResponse(responseCode = "404", description = "El usuario no tiene un carrito activo")
    })
    @GetMapping("/cart")
    public ResponseEntity<OrderResponse> getActiveOrderByUserEmail(
            @RequestParam String userEmail
    ) {
        return orderService.getActiveOrderByUserEmail(userEmail);
    }


    // Agregar ítem al carrito sin reconstruir todo
    @Operation(
            summary = "Agregar producto al carrito",
            description = "Agrega un producto al carrito (orden en estado PENDING) del usuario. " +
                    "Si el usuario aún no tiene carrito, se crea uno nuevo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Carrito creado y producto agregado"),
            @ApiResponse(responseCode = "200", description = "Producto agregado o actualizado en el carrito existente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuario o producto no encontrado")
    })
    @PostMapping("/cart/items")
    public ResponseEntity<OrderResponse> addItemToCart(
            @Valid @RequestBody AddItemToOrderRequest request
    ) {
        return orderService.addItemToCart(request);
    }

    @Operation(summary = "Actualizar estado de la orden", description = "Cambia el estado de una orden específica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado inválido"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<MessageResponse> updateDonationStatus(@PathVariable Long id, @RequestParam DonationStatus status) {
        return orderService.updateDonationStatusById(id, status);
    }

    @Operation(summary = "Eliminar una orden", description = "Elimina una orden por su ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orden eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteOrder(@PathVariable Long id) {
        return orderService.deleteOrder(id);
    }
}
