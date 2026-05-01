package com.donatech.order.client;

import com.donatech.order.dto.ProductResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalog-service", path = "/api/products", fallback = ProductClientFallback.class)
public interface ProductClient {

    @GetMapping("/{id}")
    ProductResponseDto getProductById(@PathVariable("id") String id);
}
