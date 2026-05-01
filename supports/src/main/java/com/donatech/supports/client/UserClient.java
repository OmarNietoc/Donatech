package com.donatech.supports.client;

import com.donatech.supports.dto.UsuarioDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "users-service", path = "/api/users")
public interface UserClient {

    @GetMapping("/{id}")
    UsuarioDTO getUserById(@PathVariable("id") Long id);
}
