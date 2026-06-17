package com.donatech.users.service;


import com.donatech.users.dto.AdminUserUpdateDto;
import com.donatech.users.dto.ProfileResponseDto;
import com.donatech.users.dto.ProfileUpdateDto;
import com.donatech.users.dto.UserDto;
import com.donatech.users.exception.ResourceNotFoundException;
import com.donatech.users.model.Beneficiary;
import com.donatech.users.model.CompanyDetails;
import com.donatech.users.model.User;
import com.donatech.users.model.Role;

import com.donatech.users.repository.BeneficiaryRepository;
import com.donatech.users.repository.CompanyDetailsRepository;
import com.donatech.users.repository.ComunaRepository;
import com.donatech.users.repository.RegionRepository;
import com.donatech.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final ComunaRepository comunaRepository;
    private final RegionRepository regionRepository;
    private final ImageStorageService imageStorageService;
    private final BeneficiaryRepository beneficiaryRepository;
    private final CompanyDetailsRepository companyDetailsRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        User userFound = userOpt.get();
        return userFound;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByEmailAndIdNot(String email, Long id) {
        return userRepository.existsByEmailAndIdNot(email, id);
    }

    public void saveUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo.");
        }
        userRepository.save(user);
    }

    public void deleteUserById(Long id) {
        User user = getUserById(id);
        userRepository.deleteById(id);
    }

    private Role emailAndRoleValidator(String email, Long roleId, Long userIdToExclude) {
        // Validación de correo
        if (userIdToExclude == null) {
            if (existsByEmail(email)) {
                throw new IllegalArgumentException("Ya existe un usuario con ese correo.");
            }
        } else {
            if (existsByEmailAndIdNot(email, userIdToExclude)) {
                throw new IllegalArgumentException("Ya existe otro usuario con ese correo.");
            }
        }

        // Validación de rol
        return roleService.getRoleById(roleId);
    }


    public void createUser(UserDto userDto) {

        Role role = emailAndRoleValidator(userDto.getEmail(), userDto.getRole(), null);

        User user = new User(
                userDto.getName(),
                userDto.getEmail(),
                userDto.getPassword(),
                role,
                userDto.getStatus(),
                userDto.getImagen(),
                userDto.getPhone(),
                regionRepository.getRegionById(userDto.getRegion()),
                comunaRepository.getComunaById(userDto.getComuna())
        );
        user.setApellido(userDto.getApellido());

        userRepository.save(user);
    }

    // Edición administrativa: nombre, apellido, correo, rol y estado. NO toca la contraseña.
    public void adminUpdate(Long id, AdminUserUpdateDto dto) {
        User user = getUserById(id);
        Role role = emailAndRoleValidator(dto.getEmail(), dto.getRoleId(), id);
        user.setName(dto.getName());
        user.setApellido(dto.getApellido());
        user.setEmail(dto.getEmail());
        user.setRole(role);
        user.setStatus(dto.getStatus());
        userRepository.save(user);
    }

    // ─── Perfil propio (self-service) ───────────────────────────────────────
    // Identidad por email (authentication.name). El rut NUNCA se modifica aquí.

    @Transactional(readOnly = true)
    public ProfileResponseDto getOwnProfile(String email) {
        return toProfileDto(getUserByEmail(email));
    }

    @Transactional
    public ProfileResponseDto updateOwnProfile(String email, ProfileUpdateDto dto) {
        User user = getUserByEmail(email);
        user.setName(dto.getName());
        user.setApellido(dto.getApellido());
        user.setPhone(dto.getPhone());
        user.setRegion(dto.getRegionId() != null ? regionRepository.getRegionById(dto.getRegionId()) : null);
        user.setComuna(dto.getComunaId() != null ? comunaRepository.getComunaById(dto.getComunaId()) : null);
        userRepository.save(user);

        beneficiaryRepository.findByUserId(user.getId()).ifPresent(b -> {
            b.setDireccionEntrega(dto.getDireccionEntrega());
            b.setObservaciones(dto.getObservaciones());
            beneficiaryRepository.save(b);   // rut intacto
        });

        companyDetailsRepository.findByUserId(user.getId()).ifPresent(c -> {
            if (dto.getRazonSocial() != null && !dto.getRazonSocial().isBlank()) {
                c.setRazonSocial(dto.getRazonSocial());
            }
            c.setGiro(dto.getGiro());
            c.setDireccionLegal(dto.getDireccionLegal());
            companyDetailsRepository.save(c);   // rut intacto
        });

        return toProfileDto(user);
    }

    public void updatePassword(Long id, String encodedPassword) {
        User user = getUserById(id);
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    private ProfileResponseDto toProfileDto(User user) {
        Beneficiary benef = beneficiaryRepository.findByUserId(user.getId()).orElse(null);
        CompanyDetails company = companyDetailsRepository.findByUserId(user.getId()).orElse(null);
        String rut = benef != null ? benef.getRut() : (company != null ? company.getRut() : null);
        return new ProfileResponseDto(
                user.getId(),
                user.getName(),
                user.getApellido(),
                user.getEmail(),
                user.getPhone(),
                user.getRegion() != null ? user.getRegion().getId() : null,
                user.getRegion() != null ? user.getRegion().getName() : null,
                user.getComuna() != null ? user.getComuna().getId() : null,
                user.getComuna() != null ? user.getComuna().getName() : null,
                user.getRole() != null ? user.getRole().getName() : null,
                rut,
                benef != null ? benef.getDireccionEntrega() : null,
                benef != null ? benef.getObservaciones() : null,
                company != null ? company.getRazonSocial() : null,
                company != null ? company.getGiro() : null,
                company != null ? company.getDireccionLegal() : null
        );
    }

    public void updateUserStatus(Long id, Integer status) {
        User user = getUserById(id);
        user.setStatus(status);
        userRepository.save(user);
    }

    public String uploadAvatar(Long id, MultipartFile file, String uploaderEmail) throws IOException {
        User user = getUserById(id);
        if (user.getAvatarUrl() != null) {
            imageStorageService.delete(user.getAvatarUrl());
        }
        String url = imageStorageService.store("users", uploaderEmail, file);
        user.setAvatarUrl(url);
        userRepository.save(user);
        return url;
    }

    public ResponseEntity<byte[]> getAvatar(Long id) throws IOException {
        User user = getUserById(id);
        if (user.getAvatarUrl() == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] bytes = imageStorageService.load(user.getAvatarUrl());
        String contentType = imageStorageService.detectContentType(user.getAvatarUrl());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(bytes);
    }

}
