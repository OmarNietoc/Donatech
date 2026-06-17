package com.donatech.order.client;

import com.donatech.order.dto.CompanyDetailsDto;
import org.springframework.stereotype.Component;

@Component
public class CompanyClientFallback implements CompanyClient {
    @Override
    public CompanyDetailsDto getCompany(Long userId) {
        return null;
    }
}
