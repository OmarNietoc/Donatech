package com.donatech.order.client;

import com.donatech.order.controller.response.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "users-service", path = "/api/users/internal", fallback = UserClientFallback.class)
public interface UserClient {

    @GetMapping("/by-email")
    UserResponseDto getUserByEmail(@RequestParam("email") String email);

    @GetMapping("/by-id/{id}")
    UserResponseDto getUserById(@PathVariable("id") Long id);

    @GetMapping("/contact/{id}")
    com.donatech.order.dto.ContactDto getContactById(@PathVariable("id") Long id);
}
