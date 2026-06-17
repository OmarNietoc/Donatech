package com.donatech.shipping.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "users-service", contextId = "shippingUsersClient", path = "/api/users/internal")
public interface UsersClient {

    @GetMapping("/contact/{id}")
    ContactDto getContact(@PathVariable("id") Long id);
}
