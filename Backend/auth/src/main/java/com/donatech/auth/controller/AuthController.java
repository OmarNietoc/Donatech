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
        userRequest.setEmail(request.getEmail());
        userRequest.setPassword(passwordEncoder.encode(request.getPassword()));
        userRequest.setRoleId(4L);
        userRequest.setPhone(request.getPhone());
        userRequest.setRegionId(request.getRegionId());
        userRequest.setComunaId(request.getComunaId());

        UserCredentialsDto created = userServiceClient.createUser(userRequest);

        userServiceClient.createBeneficiary(new CreateBeneficiaryInternalDto(
                created.getId(),
                request.getRut(),
                request.getDireccionEntrega(),
                request.getObservaciones()
        ));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("Beneficiario registrado exitosamente"));
    }

    @Operation(summary = "Registro organización", description = "Registra usuario con rol ROLE_ORGANIZACION y datos de empresa")
    @PostMapping("/register/organization")
    public ResponseEntity<MessageResponse> registerOrganization(@Valid @RequestBody RegisterOrganizationRequest request) {
        RegisterRequest userRequest = new RegisterRequest();
        userRequest.setName(request.getName());
        userRequest.setEmail(request.getEmail());
        userRequest.setPassword(passwordEncoder.encode(request.getPassword()));
        userRequest.setRoleId(5L);
        userRequest.setPhone(request.getPhone());
        userRequest.setRegionId(request.getRegionId());
        userRequest.setComunaId(request.getComunaId());

        UserCredentialsDto created = userServiceClient.createUser(userRequest);

        userServiceClient.createCompanyDetails(new CreateCompanyInternalDto(
                created.getId(),
                request.getRut(),
                request.getRazonSocial(),
                request.getGiro(),
                request.getDireccionLegal()
        ));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("Organización registrada exitosamente"));
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
