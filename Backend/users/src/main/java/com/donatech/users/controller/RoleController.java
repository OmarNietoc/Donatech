package com.donatech.users.controller;

import com.donatech.users.controller.response.MessageResponse;
import com.donatech.users.model.Role;
import com.donatech.users.model.User;
import com.donatech.users.repository.RoleRepository;
import com.donatech.users.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Operation(summary = "Listar todos los roles", description = "Obtiene una lista con todos los roles registrados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Roles listados correctamente",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Role.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @Operation(summary = "Obtener un rol por ID", description = "Retorna los detalles de un rol específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rol encontrado correctamente",
                    content = @Content(schema = @Schema(implementation = Role.class))),
            @ApiResponse(responseCode = "404", description = "Rol no encontrado")
    })
    @Parameter(name = "id", description = "ID del rol", required = true)
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Role role = roleService.getRoleById(id);
        return ResponseEntity.ok(role);
    }

    @Operation(summary = "Crear un nuevo rol", description = "Registra un nuevo rol en la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Rol creado exitosamente",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe un rol con ese nombre"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos enviados en la solicitud")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Objeto rol a crear",
            required = true,
            content = @Content(schema = @Schema(implementation = Role.class))
    )
    @PostMapping("/add")
    public ResponseEntity<MessageResponse> addRole(@Valid @RequestBody Role role) {
        roleService.createRole(role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("Rol creado exitosamente."));
    }

    @Operation(summary = "Actualizar rol", description = "Actualiza los datos de un rol existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rol actualizado correctamente",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "Rol no encontrado"),
            @ApiResponse(responseCode = "409", description = "Ya existe un rol con ese nombre")
    })
    @Parameter(name = "id", description = "ID del rol a actualizar", required = true)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos actualizados del rol",
            required = true,
            content = @Content(schema = @Schema(implementation = Role.class))
    )
    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> updateRole(@PathVariable Long id, @Valid @RequestBody Role roleDetails) {
        roleService.updateRole(id, roleDetails);
        return ResponseEntity.ok(new MessageResponse("Rol actualizado exitosamente."));
    }

    @Operation(summary = "Eliminar un rol", description = "Elimina un rol si no tiene usuarios asociados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rol eliminado correctamente",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "Rol no encontrado"),
            @ApiResponse(responseCode = "409", description = "No se puede eliminar el rol porque tiene usuarios asociados")
    })
    @Parameter(name = "id", description = "ID del rol a eliminar", required = true)
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteRole(@PathVariable Long id) {
        roleService.getRoleById(id);
        roleService.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("Rol eliminado exitosamente."));
    }
}

