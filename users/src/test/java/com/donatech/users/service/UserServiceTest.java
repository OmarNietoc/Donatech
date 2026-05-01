package com.donatech.users.service;

import com.donatech.users.dto.UserDto;
import com.donatech.users.model.Role;
import com.donatech.users.model.User;
import com.donatech.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.donatech.users.exception.ResourceNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private UserService userService;

    private UserDto createValidUserDto() {
        UserDto dto = new UserDto();
        dto.setName("Juan Pérez");
        dto.setEmail("juan@edutech.com");
        dto.setPassword("Password123");
        dto.setRole(1L);
        dto.setStatus(1);
        return dto;
    }

    private User createValidUser() {
        Role role = new Role(1L, "ADMIN");
        User user = new User(
                "Juan Pérez",
                "juan@edutech.com",
                "Password123",
                role,
                1,
                null, // imagen
                null, // firebaseId
                null, // phone
                null, // region
                null // comuna
        );
        user.setId(1L);
        return user;
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Arrange
        User user1 = createValidUser();
        User user2 = createValidUser();
        user2.setId(2L);
        user2.setEmail("maria@edutech.com");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        // Act
        List<User> users = userService.getAllUsers();

        // Assert
        assertEquals(2, users.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // Arrange
        Long userId = 1L;
        User mockUser = createValidUser();
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Act
        User result = userService.getUserById(userId);

        // Assert
        assertEquals("juan@edutech.com", result.getEmail());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUserById_WhenUserNotExists_ShouldThrowException() {
        // Arrange
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(userId);
        });
    }

    @Test
    void createUser_WithValidData_ShouldSaveUser() {
        // Arrange
        UserDto userDto = createValidUserDto();
        Role mockRole = new Role(1L, "ADMIN");

        when(roleService.getRoleById(1L)).thenReturn(mockRole);
        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        userService.createUser(userDto);

        // Assert
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_WithDuplicateEmail_ShouldThrowException() {
        // Arrange
        UserDto userDto = createValidUserDto();
        when(userRepository.existsByEmail("juan@edutech.com")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(userDto);
        });
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_WithValidData_ShouldUpdateUser() {
        // Arrange
        Long userId = 1L;
        User existingUser = createValidUser();
        UserDto updateDto = createValidUserDto();
        updateDto.setName("Juan Pérez Actualizado");
        updateDto.setEmail("nuevo@edutech.com");

        Role mockRole = new Role(1L, "ADMIN");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(roleService.getRoleById(1L)).thenReturn(mockRole);
        when(userRepository.existsByEmailAndIdNot("nuevo@edutech.com", userId)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        userService.updateUser(userId, updateDto);

        // Assert
        assertEquals("Juan Pérez Actualizado", existingUser.getName());
        assertEquals("nuevo@edutech.com", existingUser.getEmail());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void updateUser_WithDuplicateEmail_ShouldThrowException() {
        // Arrange
        Long userId = 1L;
        User existingUser = createValidUser();
        UserDto updateDto = createValidUserDto();
        updateDto.setEmail("existente@edutech.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmailAndIdNot("existente@edutech.com", userId)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(userId, updateDto);
        });
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUserById_WhenUserExists_ShouldDelete() {
        // Arrange
        Long userId = 1L;
        User mockUser = createValidUser();
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Act
        userService.deleteUserById(userId);

        // Assert
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void existsByEmail_ShouldReturnCorrectBoolean() {
        // Arrange
        when(userRepository.existsByEmail("test@edutech.com")).thenReturn(true);

        // Act & Assert
        assertTrue(userService.existsByEmail("test@edutech.com"));
        verify(userRepository, times(1)).existsByEmail("test@edutech.com");
    }

    @Test
    void saveUser_ShouldCallRepositorySave() {
        // Arrange
        User user = createValidUser();
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        userService.saveUser(user);

        // Assert
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void saveUser_WithNullUser_ShouldThrowException() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.saveUser(null);
        });
        verify(userRepository, never()).save(any());
    }
}
