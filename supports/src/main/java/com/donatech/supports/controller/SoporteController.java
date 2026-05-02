package com.donatech.supports.controller;

import com.donatech.supports.controller.response.MessageResponse;
import com.donatech.supports.dto.EstadoDTO;
import com.donatech.supports.dto.ResponderDTO;
import com.donatech.supports.dto.SoporteRequestDTO;
import com.donatech.supports.model.EstadoSoporte;
import com.donatech.supports.model.Soporte;
import com.donatech.supports.model.TipoSoporte;
import com.donatech.supports.service.SoporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supports")
@RequiredArgsConstructor
@Tag(name = "Soporte", description = "Gestión de tickets de soporte")
public class SoporteController {

    private final SoporteService soporteService;

    @Operation(summary = "Crear ticket de soporte")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ticket creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping
    public ResponseEntity<Soporte> crear(@Valid @RequestBody SoporteRequestDTO dto) {
        return soporteService.crear(dto);
    }

    @Operation(summary = "Listar todos los tickets")
    @GetMapping
    public List<Soporte> obtenerTodas() {
        return soporteService.obtenerTodas();
    }

    @Operation(summary = "Obtener ticket por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket encontrado"),
            @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Soporte> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(soporteService.obtenerSoportePorId(id));
    }

    @Operation(summary = "Tickets por estado")
    @GetMapping("/by-estado")
    public List<Soporte> getByEstado(@RequestParam EstadoSoporte estado) {
        return soporteService.getByEstado(estado);
    }

    @Operation(summary = "Tickets por tipo")
    @GetMapping("/by-tipo")
    public List<Soporte> getByTipo(@RequestParam TipoSoporte tipo) {
        return soporteService.getByTipo(tipo);
    }

    @Operation(summary = "Tickets por usuario")
    @GetMapping("/by-usuario/{usuarioId}")
    public List<Soporte> getByUsuario(@PathVariable Long usuarioId) {
        return soporteService.getByUsuario(usuarioId);
    }

    @Operation(summary = "Tickets asignados a voluntario")
    @GetMapping("/by-voluntario/{voluntarioId}")
    public List<Soporte> getByVoluntario(@PathVariable Long voluntarioId) {
        return soporteService.getByVoluntario(voluntarioId);
    }

    @Operation(summary = "Tickets por donación")
    @GetMapping("/by-donation/{donationId}")
    public List<Soporte> getByDonation(@PathVariable Long donationId) {
        return soporteService.getByDonation(donationId);
    }

    @Operation(summary = "Actualizar estado del ticket")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<Soporte> actualizarEstado(@PathVariable Long id, @Valid @RequestBody EstadoDTO dto) {
        return soporteService.actualizarEstado(id, dto.getEstado());
    }

    @Operation(summary = "Asignar ticket a voluntario")
    @PatchMapping("/{id}/assign")
    public ResponseEntity<Soporte> asignar(@PathVariable Long id, @RequestParam Long voluntarioId) {
        return soporteService.asignar(id, voluntarioId);
    }

    @Operation(summary = "Responder ticket y marcar como resuelto")
    @PatchMapping("/{id}/respond")
    public ResponseEntity<Soporte> responder(@PathVariable Long id, @Valid @RequestBody ResponderDTO dto) {
        return soporteService.responder(id, dto);
    }

    @Operation(summary = "Validar campaña",
            description = "Admin/voluntario aprueba o rechaza una campaña. Publica evento a catalog ms.")
    @PatchMapping("/{id}/validate-campaign")
    public ResponseEntity<MessageResponse> validateCampaign(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @RequestParam(required = false, defaultValue = "") String motivo) {
        return soporteService.validateCampaign(id, approved, motivo);
    }

    @Operation(summary = "Validar transferencia bancaria",
            description = "Admin/voluntario aprueba o rechaza comprobante. Publica evento a order ms.")
    @PatchMapping("/{id}/validate-transfer")
    public ResponseEntity<MessageResponse> validateTransfer(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @RequestParam(required = false, defaultValue = "") String motivo) {
        return soporteService.validateTransfer(id, approved, motivo);
    }

    @Operation(summary = "Eliminar ticket de soporte")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket eliminado"),
            @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> eliminar(@PathVariable Long id) {
        return soporteService.eliminar(id);
    }
}
