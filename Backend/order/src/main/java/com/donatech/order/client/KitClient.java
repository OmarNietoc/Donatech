package com.donatech.order.client;

import com.donatech.order.dto.KitResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalog-service", contextId = "kitClient", path = "/api/kits", fallback = KitClientFallback.class)
public interface KitClient {

    @GetMapping("/{id}")
    KitResponseDto getKitById(@PathVariable("id") Long id);
}
