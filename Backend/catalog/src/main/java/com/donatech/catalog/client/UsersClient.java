package com.donatech.catalog.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "users-service", path = "/api/users", fallback = UsersClientFallback.class)
public interface UsersClient {

    @GetMapping("/{id}")
    UserStatusDto getUserById(@PathVariable("id") Long id);
}
