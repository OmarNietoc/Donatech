package com.donatech.order.client;

import com.donatech.order.dto.CompanyDetailsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "users-service", contextId = "orderCompanyClient",
        path = "/api/users/internal", fallback = CompanyClientFallback.class)
public interface CompanyClient {

    @GetMapping("/company/{userId}")
    CompanyDetailsDto getCompany(@PathVariable("userId") Long userId);
}
