package com.donatech.users.service;

import com.donatech.users.exception.ConflictException;
import com.donatech.users.exception.ResourceNotFoundException;
import com.donatech.users.model.Role;
import com.donatech.users.repository.RoleRepository;
import com.donatech.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoleService roleService;

    @Test
    void getAllRoles_ShouldReturnAllRoles() {
        // Arrange
        Role adminRole = new Role(1L, "ADMIN");
        Role userRole = new Role(2L, "USER");
        when(roleRepository.findAll()).thenReturn(Arrays.asList(adminRole, userRole));

        // Act
        List<Role> roles = roleService.getAllRoles();

        // Assert
        assertEquals(2, roles.size());
        assertTrue(roles.stream().anyMatch(r -> r.getName().equals("ADMIN")));
        verify(roleRepository, times(1)).findAll();
    }

    @Test
    void getRoleById_WhenRoleExists_ShouldReturnRole() {
        // Arrange
        Long roleId = 1L;
        Role mockRole = new Role(roleId, "ADMIN");
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(mockRole));

        // Act
        Role result = roleService.getRoleById(roleId);

        // Assert
        assertEquals("ADMIN", result.getName());
        verify(roleRepository, times(1)).findById(roleId);
    }

    @Test
    void getRoleById_WhenRoleNotExists_ShouldThrowException() {
        // Arrange
        Long roleId = 99L;
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            roleService.getRoleById(roleId);
        });
    }

    @Test
    void existsById_ShouldReturnTrueWhenRoleExists() {
        // Arrange
        Long roleId = 1L;
        when(roleRepository.existsById(roleId)).thenReturn(true);

        // Act & Assert
        assertTrue(roleService.existsById(roleId));
    }

    @Test
    void validateRoleByName_WhenNameExists_ShouldThrowException() {
        // Arrange
        String roleName = "ADMIN";
        when(roleRepository.existsByNameIgnoreCase(roleName)).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            roleService.validateRoleByName(roleName);
        });
    }

    @Test
    void createRole_WithValidData_ShouldSaveRole() {
        // Arrange
        Role newRole = new Role(null, "EDITOR");
        Role savedRole = new Role(1L, "EDITOR");
        when(roleRepository.existsByNameIgnoreCase("EDITOR")).thenReturn(false);
        when(roleRepository.save(newRole)).thenReturn(savedRole);

        // Act
        Role result = roleService.createRole(newRole);

        // Assert
        assertEquals(1L, result.getId());
        verify(roleRepository, times(1)).save(newRole);
    }

    @Test
    void deleteById_WhenNoUsersAssociated_ShouldDelete() {
        // Arrange
        Long roleId = 1L;
        Role mockRole = new Role(roleId, "ADMIN");
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(mockRole));
        when(userRepository.existsUserByRoleId(roleId)).thenReturn(false);

        // Act
        roleService.deleteById(roleId);

        // Assert
        verify(roleRepository, times(1)).deleteById(roleId);
    }

    @Test
    void deleteById_WhenUsersAssociated_ShouldThrowException() {
        // Arrange
        Long roleId = 1L;
        Role mockRole = new Role(roleId, "ADMIN");
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(mockRole));
        when(userRepository.existsUserByRoleId(roleId)).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            roleService.deleteById(roleId);
        });
        verify(roleRepository, never()).deleteById(any());
    }

    @Test
    void updateRole_WithValidName_ShouldUpdate() {
        // Arrange
        Long roleId = 1L;
        Role existingRole = new Role(roleId, "OLD_NAME");
        Role updatedDetails = new Role(null, "NEW_NAME");

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existingRole));
        when(roleRepository.existsByNameIgnoreCase("NEW_NAME")).thenReturn(false);

        // Act
        roleService.updateRole(roleId, updatedDetails);

        // Assert
        assertEquals("NEW_NAME", existingRole.getName());
        verify(roleRepository, times(1)).save(existingRole);
    }

    @Test
    void updateRole_WithDuplicateName_ShouldThrowException() {
        // Arrange
        Long roleId = 1L;
        Role existingRole = new Role(roleId, "ADMIN");
        Role updatedDetails = new Role(null, "EXISTING_NAME");

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existingRole));
        when(roleRepository.existsByNameIgnoreCase("EXISTING_NAME")).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            roleService.updateRole(roleId, updatedDetails);
        });
        verify(roleRepository, never()).save(any());
    }

    @Test
    void validateRoleByName_lanzaExcepcionCuandoYaExiste() {
        String nombreDuplicado = "ADMIN";

        when(roleRepository.existsByNameIgnoreCase(nombreDuplicado)).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> roleService.validateRoleByName(nombreDuplicado)
        );

        assertEquals("Ya existe un rol con ese nombre.", exception.getMessage());
    }
}