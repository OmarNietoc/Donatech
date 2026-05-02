package com.donatech.order.client;

import com.donatech.order.dto.KitResponseDto;
import org.springframework.stereotype.Component;

@Component
public class KitClientFallback implements KitClient {

    @Override
    public KitResponseDto getKitById(Long id) {
        return null;
    }
}
