package com.donatech.users.controller;

import com.donatech.users.controller.response.MessageResponse;
import com.donatech.users.dto.AdminUserUpdateDto;
import com.donatech.users.dto.ProfileResponseDto;
import com.donatech.users.dto.ProfileUpdateDto;
import com.donatech.users.dto.UserDto;
import com.donatech.users.repository.RoleRepository;
import com.donatech.users.service.RoleService;
import com.donatech.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.donatech.users.repository.UserRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.donatech.users.model.User;
import com.donatech.users.model.Role;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public record CollaboratorDto(Long id, String name, String email) {}

    @Operation(summary = "Listar colaboradores (voluntarios activos)",
            description = "Para asignación de entregas. Devuelve voluntarios activos.")
    @GetMapping("/collaborators")
    @PreAuthorize("hasAnyRole('ADMIN','VOLUNTARIO')")
    public ResponseEntity<List<CollaboratorDto>> getCollaborators() {
        List<CollaboratorDto> list = userRepository.findByRole_NameAndStatus("ROLE_VOLUNTARIO", 1).stream()
                .map(u -> new CollaboratorDto(u.getId(), u.getName(), u.getEmail()))
                .toList();
        return ResponseEntity.ok(list);
    }

    // ─── Perfil propio (cualquier usuario autenticado) ──────────────────────

    @Operation(summary = "Obtener mi perfil", description = "Datos del usuario autenticado (incluye datos de beneficiario/empresa si aplica).")
    @GetMapping("/me")
    public ResponseEntity<ProfileResponseDto> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getOwnProfile(authentication.getName()));
    }

    @Operation(summary = "Actualizar mi perfil", description = "Edita los datos propios. El email, rol y RUT no se pueden modificar.")
    @PutMapping("/me")
    public ResponseEntity<ProfileResponseDto> updateMyProfile(
            @Valid @RequestBody ProfileUpdateDto dto, Authentication authentication) {
        return ResponseEntity.ok(userService.updateOwnProfile(authentication.getName(), dto));
    }

    @Operation(summary = "Subir mi avatar")
    @PostMapping(value = "/me/avatar", consumes = "multipart/form-data")
    public ResponseEntity<MessageResponse> uploadMyAvatar(
            @RequestParam("file") MultipartFile file, Authentication authentication) throws IOException {
        String email = authentication.getName();
        Long id = userService.getUserByEmail(email).getId();
        userService.uploadAvatar(id, file, email);
        return ResponseEntity.ok(new MessageResponse("Avatar actualizado correctamente."));
    }

    @Operation(summary = "Listar todos los usuarios", description = "Retorna todos los usuarios registrados en la plataforma.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuarios listados correctamente",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = User.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "Obtener usuario por ID", description = "Retorna los detalles de un usuario específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado correctamente",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Obtener usuario por email", description = "Retorna los detalles de un usuario especificado por su correo electrónico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado correctamente",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/by-email")
    @PreAuthorize("hasRole('ADMIN') or #email == authentication.name")
    public ResponseEntity<User> getUserByEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @Operation(summary = "Agregar nuevo usuario", description = "Crea un nuevo usuario en la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos enviados en la solicitud")
    })
    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> addUser(@Valid @RequestBody UserDto userDto) {
        userService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("Usuario agregado exitosamente."));
    }

    @Operation(summary = "Actualizar usuario por ID", description = "Actualiza la información de un usuario existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> updateUser(@PathVariable Long id, @Valid @RequestBody AdminUserUpdateDto userDetails) {
        userService.adminUpdate(id, userDetails);
        return ResponseEntity.ok(new MessageResponse("Usuario actualizado exitosamente."));
    }

    @Operation(summary = "Eliminar usuario por ID", description = "Elimina un usuario específico de la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario eliminado correctamente",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.ok(new MessageResponse("Usuario eliminado correctamente."));
    }

    @Operation(summary = "Actualizar estado del usuario", description = "Actualiza el estado (activo/inactivo) de un usuario.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado del usuario actualizado correctamente",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Estado inválido"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (status < 0 || status > 1) {
            return ResponseEntity.badRequest().body(new MessageResponse("El 'status' debe ser 1 o 0."));
        }
        userService.updateUserStatus(id, status);
        return ResponseEntity.ok(new MessageResponse("Estado del usuario actualizado correctamente."));
    }

    @Operation(summary = "Subir avatar de usuario")
    @PostMapping(value = "/{id}/avatar", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<MessageResponse> uploadAvatar(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-User-Email", defaultValue = "unknown") String uploaderEmail) throws IOException {
        userService.uploadAvatar(id, file, uploaderEmail);
        return ResponseEntity.ok(new MessageResponse("Avatar actualizado correctamente."));
    }

    @Operation(summary = "Obtener avatar de usuario")
    @GetMapping("/{id}/avatar")
    public ResponseEntity<byte[]> getAvatar(@PathVariable Long id) throws IOException {
        return userService.getAvatar(id);
    }

}
