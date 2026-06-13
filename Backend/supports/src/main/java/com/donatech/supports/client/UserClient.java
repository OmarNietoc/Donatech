package com.donatech.supports.client;

import com.donatech.supports.dto.UsuarioDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "users-service", path = "/api/users/internal", fallback = UserClientFallback.class)
public interface UserClient {

    @GetMapping("/by-id/{id}")
    UsuarioDTO getUserById(@PathVariable("id") Long id);
}
