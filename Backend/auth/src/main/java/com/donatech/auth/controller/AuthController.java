package com.donatech.auth.controller;

import com.donatech.auth.client.UserServiceClient;
import com.donatech.auth.dto.*;
import com.donatech.auth.dto.CreateCompanyInternalDto;
import com.donatech.auth.dto.RegisterOrganizationRequest;
import com.donatech.auth.security.jwt.JwtUtils;
import com.donatech.auth.security.services.UserDetailsImpl;
import com.donatech.auth.util.RutValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Auth", description = "Autenticación y registro de usuarios")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final UserServiceClient userServiceClient;

    @Operation(summary = "Login", description = "Autentica un usuario y retorna un JWT")
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getEmail(), roles));
    }

    @Operation(summary = "Registro", description = "Registra un nuevo usuario")
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        userServiceClient.createUser(request);
        return ResponseEntity.ok(new MessageResponse("Usuario registrado exitosamente"));
    }

    @Operation(summary = "Registro beneficiario", description = "Registra usuario con perfil de beneficiario — valida RUT mod 11")
    @PostMapping("/register/beneficiary")
    public ResponseEntity<MessageResponse> registerBeneficiary(@Valid @RequestBody RegisterBeneficiaryRequest request) {
        if (!RutValidator.isValid(request.getRut())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RUT inválido: " + request.getRut());
        }
        RegisterRequest userRequest = new RegisterRequest();
        userRequest.setName(request.getName());
        userRequest.setApellido(request.getApellido());
        userRequest.setEmail(request.getEmail());
        userRequest.setPassword(passwordEncoder.encode(request.getPassword()));
        userRequest.setRoleId(4L);
        userRequest.setPhone(request.getPhone());
        userRequest.setRegionId(request.getRegionId());
        userRequest.setComunaId(request.getComunaId());

        UserCredentialsDto created = userServiceClient.createUser(userRequest);

        try {
            userServiceClient.createBeneficiary(new CreateBeneficiaryInternalDto(
                    created.getId(),
                    request.getRut(),
                    request.getDireccionEntrega(),
                    request.getObservaciones()
            ));
        } catch (RuntimeException ex) {
            // Compensación: la cuenta ya se creó pero falló el perfil → borrar cuenta huérfana.
            safeDeleteUser(created.getId());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo completar el registro del beneficiario. Intenta nuevamente.");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("Beneficiario registrado exitosamente"));
    }

    @Operation(summary = "Registro organización", description = "Registra usuario con rol ROLE_ORGANIZACION y datos de empresa")
    @PostMapping("/register/organization")
    public ResponseEntity<MessageResponse> registerOrganization(@Valid @RequestBody RegisterOrganizationRequest request) {
        RegisterRequest userRequest = new RegisterRequest();
        userRequest.setName(request.getName());
        userRequest.setApellido(request.getApellido());
        userRequest.setEmail(request.getEmail());
        userRequest.setPassword(passwordEncoder.encode(request.getPassword()));
        userRequest.setRoleId(5L);
        userRequest.setPhone(request.getPhone());
        userRequest.setRegionId(request.getRegionId());
        userRequest.setComunaId(request.getComunaId());

        UserCredentialsDto created = userServiceClient.createUser(userRequest);

        try {
            userServiceClient.createCompanyDetails(new CreateCompanyInternalDto(
                    created.getId(),
                    request.getRut(),
                    request.getRazonSocial(),
                    request.getGiro(),
                    request.getDireccionLegal()
            ));
        } catch (RuntimeException ex) {
            // Compensación: borrar la cuenta huérfana si falló la creación del perfil de empresa.
            safeDeleteUser(created.getId());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo completar el registro de la organización. Intenta nuevamente.");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("Organización registrada exitosamente"));
    }

    // Borra la cuenta recién creada como compensación; si la compensación misma falla,
    // no enmascara el error original (solo registra y sigue).
    private void safeDeleteUser(Long userId) {
        try {
            userServiceClient.deleteUser(userId);
        } catch (RuntimeException ignored) {
            // best-effort: el error de registro se propaga igualmente al cliente.
        }
    }

    @Operation(summary = "Cambiar contraseña", description = "El usuario autenticado cambia su contraseña verificando la actual.")
    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request, Authentication authentication) {
        String email = authentication.getName();
        UserCredentialsDto creds = userServiceClient.getCredentialsByEmail(email);
        if (creds == null || !passwordEncoder.matches(request.getCurrentPassword(), creds.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña actual no es correcta.");
        }
        userServiceClient.updatePassword(creds.getId(),
                Map.of("password", passwordEncoder.encode(request.getNewPassword())));
        return ResponseEntity.ok(new MessageResponse("Contraseña actualizada correctamente."));
    }

    @Operation(summary = "Refresh token", description = "Genera nuevo JWT con el token actual válido")
    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String newToken = jwtUtils.generateJwtToken(authentication);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList());
        return ResponseEntity.ok(new JwtResponse(newToken, userDetails.getId(), userDetails.getEmail(), roles));
    }

    @Operation(summary = "Validar token", description = "Valida el JWT — retorna 200 si válido, 401 si no")
    @GetMapping("/validate")
    public ResponseEntity<Void> validate() {
        return ResponseEntity.ok().build();
    }
}
