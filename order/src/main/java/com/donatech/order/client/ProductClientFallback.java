package com.donatech.order.client;

import com.donatech.order.dto.ProductResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ProductClientFallback implements ProductClient {

    @Override
    public ProductResponseDto getProductById(String id) {
        return null;
    }
}
