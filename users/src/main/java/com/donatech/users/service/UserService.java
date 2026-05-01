package com.donatech.users.service;


import com.donatech.users.dto.UserDto;
import com.donatech.users.exception.ResourceNotFoundException;
import com.donatech.users.model.User;
import com.donatech.users.model.Role;

import com.donatech.users.repository.ComunaRepository;
import com.donatech.users.repository.RegionRepository;
import com.donatech.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final RoleService roleService;

    private final ComunaRepository comunaRepository;

    private final RegionRepository regionRepository;

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
                userDto.getFirebaseId(),
                userDto.getPhone(),
                regionRepository.getRegionById(userDto.getRegion()),
                comunaRepository.getComunaById(userDto.getComuna())

        );

        userRepository.save(user);
    }

    public void updateUser(Long id, UserDto userDetails) {

        User user = getUserById(id);

        Role role = emailAndRoleValidator(userDetails.getEmail(), userDetails.getRole(), id);

        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        user.setPassword(userDetails.getPassword());
        user.setStatus(userDetails.getStatus());
        user.setRole(role);
        user.setImagen(userDetails.getImagen());
        user.setFirebaseId(userDetails.getFirebaseId());

        userRepository.save(user);
    }

    public void updateUserStatus(Long id, Integer status) {
        User user = getUserById(id);
        user.setStatus(status);
        userRepository.save(user);
    }

}
