package com.donatech.users.service;

import com.donatech.users.exception.ConflictException;
import com.donatech.users.exception.ResourceNotFoundException;
import com.donatech.users.model.Role;

import com.donatech.users.repository.RoleRepository;
import com.donatech.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleById(Long id) {
        Optional<Role> roleOpt = roleRepository.findById(id);
        if (roleOpt.isEmpty()) {
            throw new ResourceNotFoundException("Role no encontrado");
        }
        Role roleFound = roleOpt.get();
        return roleFound;
    }

    public boolean existsById(Long id) {
        return roleRepository.existsById(id);
    }

    public boolean validateRoleByName(String name) {
        if (roleRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Ya existe un rol con ese nombre.");
        }
        return true;
    }

    public Role createRole(Role role) {
        validateRoleByName(role.getName());
        return roleRepository.save(role);
    }

    public void deleteById(Long id) {
        Role role = getRoleById(id);
        if (userRepository.existsUserByRoleId(id)) {
            throw new ConflictException("No se puede eliminar el rol porque hay usuarios asociados.");
        }

        roleRepository.deleteById(id);
    }

    public void updateRole(Long id, Role roleDetails) {
        Role existingRole = getRoleById(id);
        validateRoleByName(roleDetails.getName());
        existingRole.setName(roleDetails.getName());
        roleRepository.save(existingRole);
    }

}

