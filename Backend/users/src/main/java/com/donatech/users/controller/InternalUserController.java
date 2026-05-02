package com.donatech.users.controller;

import com.donatech.users.dto.BeneficiaryDto;
import com.donatech.users.dto.CreateUserInternalDto;
import com.donatech.users.dto.UserCredentialsDto;
import com.donatech.users.exception.ConflictException;
import com.donatech.users.exception.ResourceNotFoundException;
import com.donatech.users.model.Beneficiary;
import com.donatech.users.model.Region;
import com.donatech.users.model.Role;
import com.donatech.users.model.User;
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
    private final BeneficiaryService beneficiaryService;

    @GetMapping("/credentials")
    public ResponseEntity<UserCredentialsDto> getCredentialsByEmail(@RequestParam String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + email));
        return ResponseEntity.ok(toCredentialsDto(user));
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
                role, 1, null, null, dto.getPhone(), region, comuna);

        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(toCredentialsDto(user));
    }

    @PostMapping("/beneficiary")
    public ResponseEntity<Map<String, Long>> createBeneficiary(@Valid @RequestBody BeneficiaryDto dto) {
        Beneficiary b = beneficiaryService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("beneficiaryId", b.getId(), "userId", b.getUser().getId()));
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
