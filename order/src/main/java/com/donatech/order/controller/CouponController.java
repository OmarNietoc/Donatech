package com.donatech.order.controller;

import com.donatech.order.controller.response.MessageResponse;
import com.donatech.order.model.Coupon;
import com.donatech.order.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    // Obtener todos los cupones
    @Operation(summary = "Obtener todos los cupones", description = "Retorna una lista con todos los cupones disponibles.")
    @ApiResponse(responseCode = "200", description = "Lista de cupones retornada correctamente")

    @GetMapping
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return couponService.getAllCoupons();
    }

    // Obtener cupón por ID
    @Operation(summary = "Obtener cupón por ID", description = "Retorna la información de un cupón según su ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cupón encontrado"),
            @ApiResponse(responseCode = "404", description = "Cupón no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Coupon> getCouponById(@PathVariable Long id) {
        Coupon coupon = couponService.getCouponById(id);
        return ResponseEntity.ok(coupon);
    }

    // Obtener cupón por código
    @Operation(summary = "Obtener cupón por código", description = "Retorna un cupón según su código.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cupón encontrado"),
            @ApiResponse(responseCode = "404", description = "Cupón no encontrado")
    })
    @GetMapping("/by-code/{code}")
    public ResponseEntity<Coupon> getCouponByCode(@PathVariable String code) {
        Coupon coupon = couponService.getCouponByCode(code);
        return ResponseEntity.ok(coupon);
    }

    // Crear cupón pasando solo el monto del descuento
    @Operation(summary = "Crear nuevo cupón", description = "Crea un cupón recibiendo solo el monto del descuento como parámetro.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cupón creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Monto de descuento inválido")
    })
    @PostMapping("/add")
    public ResponseEntity<Coupon> createCoupon(@RequestParam Integer discountAmount) {
        return couponService.createCoupon(discountAmount);
    }

    // Actualizar un cupón existente
    @Operation(summary = "Actualizar cupón", description = "Actualiza los datos de un cupón existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cupón actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cupón no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud")
    })
    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> updateCoupon(
            @PathVariable Long id,
            @Valid @RequestBody Coupon updatedCoupon) {
        return couponService.updateCouponById(id, updatedCoupon);
    }

    // Eliminar un cupón por ID
    @Operation(summary = "Eliminar cupón", description = "Elimina un cupón según su ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cupón eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cupón no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteCoupon(@PathVariable Long id) {
        return couponService.deleteCoupon(id);
    }

    // Actualizar el estado de un cupón por código
    @Operation(summary = "Actualizar estado del cupón", description = "Cambia el estado activo/inactivo de un cupón usando su código.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado del cupón actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cupón no encontrado")
    })
    @PatchMapping("/{code}/status")
    public ResponseEntity<?> updateCouponStatus(
            @PathVariable String code,
            @RequestParam boolean active
    ) {
        return couponService.updateCouponStatusByCode(code, active);
    }

}
