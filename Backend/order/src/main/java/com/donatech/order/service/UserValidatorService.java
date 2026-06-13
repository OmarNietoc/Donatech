package com.donatech.order.service;

import com.donatech.order.client.UserClient;
import com.donatech.order.controller.response.UserResponseDto;
import com.donatech.order.exception.ResourceNotFoundException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserValidatorService {

    private final UserClient userClient;

    public UserResponseDto getUserByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("El email del usuario es obligatorio.");
        }

        try {
            UserResponseDto user = userClient.getUserByEmail(email);
            if (user == null) {
                // El fallback de Resilience4j devuelve null cuando users-service no responde
                throw new IllegalArgumentException(
                        "No se pudo validar el usuario " + email + ". Servicio de usuarios no disponible, intenta más tarde.");
            }
            if (user.getStatus() != null && user.getStatus() == 0) {
                throw new IllegalArgumentException("El usuario con email " + email + " no está activo.");
            }
            return user;
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("El usuario no existe: " + email);
        } catch (FeignException e) {
            throw new IllegalArgumentException("Error al obtener el usuario: " + e.getMessage());
        }
    }

    // Lectura tolerante a fallos para enriquecer respuestas (no lanza si el usuario no existe / ms caído)
    public UserResponseDto getUserByIdSafe(Long id) {
        if (id == null) return null;
        try {
            return userClient.getUserById(id);
        } catch (RuntimeException e) {
            return null;
        }
    }

    public com.donatech.order.dto.ContactDto getContactByIdSafe(Long id) {
        if (id == null) return null;
        try {
            return userClient.getContactById(id);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
