package com.donatech.users.controller;

import com.donatech.users.dto.BeneficiaryDto;
import com.donatech.users.dto.CompanyDetailsDto;
import com.donatech.users.dto.ContactDto;
import com.donatech.users.dto.CreateUserInternalDto;
import com.donatech.users.dto.UserCredentialsDto;
import com.donatech.users.dto.UserSummaryDto;
import com.donatech.users.exception.ConflictException;
import com.donatech.users.exception.ResourceNotFoundException;
import com.donatech.users.model.Beneficiary;
import com.donatech.users.model.Region;
import com.donatech.users.model.Role;
import com.donatech.users.model.User;
import com.donatech.users.repository.BeneficiaryRepository;
import com.donatech.users.repository.ComunaRepository;
import com.donatech.users.repository.RegionRepository;
import com.donatech.users.repository.RoleRepository;
import com.donatech.users.repository.UserRepository;
import com.donatech.users.service.BeneficiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users/internal")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RegionRepository regionRepository;
    private final ComunaRepository comunaRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final BeneficiaryService beneficiaryService;
    private final com.donatech.users.service.CompanyDetailsService companyDetailsService;

    @GetMapping("/credentials")
    public ResponseEntity<UserCredentialsDto> getCredentialsByEmail(@RequestParam String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + email));
        return ResponseEntity.ok(toCredentialsDto(user));
    }

    @GetMapping("/by-email")
    public ResponseEntity<UserSummaryDto> getSummaryByEmail(@RequestParam String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + email));
        return ResponseEntity.ok(toSummaryDto(user));
    }

    @GetMapping("/by-id/{id}")
    public ResponseEntity<UserSummaryDto> getSummaryById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
        return ResponseEntity.ok(toSummaryDto(user));
    }

    private UserSummaryDto toSummaryDto(User user) {
        return UserSummaryDto.of(user.getId(), user.getName(), user.getEmail(), user.getStatus());
    }

    @GetMapping("/contact/{id}")
    public ResponseEntity<ContactDto> getContact(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
        String direccion = beneficiaryRepository.findByUserId(id)
                .map(Beneficiary::getDireccionEntrega)
                .orElse(null);
        String comuna = user.getComuna() != null ? user.getComuna().getName() : null;
        String region = user.getRegion() != null ? user.getRegion().getName() : null;
        return ResponseEntity.ok(new ContactDto(
                user.getId(), user.getName(), user.getApellido(), user.getEmail(),
                user.getPhone(), direccion, comuna, region));
    }

    @PostMapping("/create")
    public ResponseEntity<UserCredentialsDto> createUser(@Valid @RequestBody CreateUserInternalDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("Email ya registrado: " + dto.getEmail());
        }
        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + dto.getRoleId()));

        Region region = dto.getRegionId() != null
                ? regionRepository.findById(dto.getRegionId()).orElse(null) : null;

        var comuna = dto.getComunaId() != null
                ? comunaRepository.findById(dto.getComunaId()).orElse(null) : null;

        User user = new User(dto.getName(), dto.getEmail(), dto.getPassword(),
                role, 1, null, dto.getPhone(), region, comuna);
        user.setApellido(dto.getApellido());

        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(toCredentialsDto(user));
    }

    @PostMapping("/company")
    public ResponseEntity<Map<String, Long>> createCompanyInternal(@Valid @RequestBody CompanyDetailsDto dto) {
        companyDetailsService.createOrUpdate(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("userId", dto.getUserId()));
    }

    // Datos de empresa por userId — consumido por order-service (Feign) para el certificado.
    @GetMapping("/company/{userId}")
    public ResponseEntity<com.donatech.users.model.CompanyDetails> getCompanyInternal(@PathVariable Long userId) {
        return companyDetailsService.getByUserId(userId);
    }

    @PostMapping("/beneficiary")
    public ResponseEntity<Map<String, Long>> createBeneficiary(@Valid @RequestBody BeneficiaryDto dto) {
        Beneficiary b = beneficiaryService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("beneficiaryId", b.getId(), "userId", b.getUser().getId()));
    }

    // Cambia el hash de contraseña (lo envía auth ya encriptado tras verificar la actual).
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
        user.setPassword(body.get("password"));
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    // Compensación de registro distribuido: si auth crea la cuenta pero falla la creación
    // del perfil (beneficiario/empresa), borra la cuenta huérfana para no dejar inconsistencia.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        beneficiaryRepository.findByUserId(id).ifPresent(beneficiaryRepository::delete);
        userRepository.findById(id).ifPresent(userRepository::delete);
        return ResponseEntity.noContent().build();
    }

    private UserCredentialsDto toCredentialsDto(User user) {
        return new UserCredentialsDto(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole().getName(),
                user.getStatus());
    }
}
