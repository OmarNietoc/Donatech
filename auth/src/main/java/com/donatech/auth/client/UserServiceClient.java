package com.donatech.auth.client;

import com.donatech.auth.dto.CreateBeneficiaryInternalDto;
import com.donatech.auth.dto.RegisterRequest;
import com.donatech.auth.dto.UserCredentialsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "users-service", path = "/api/users")
public interface UserServiceClient {

    @GetMapping("/internal/credentials")
    UserCredentialsDto getCredentialsByEmail(@RequestParam("email") String email);

    @PostMapping("/internal/create")
    UserCredentialsDto createUser(@RequestBody RegisterRequest request);

    @PostMapping("/internal/beneficiary")
    Map<String, Long> createBeneficiary(@RequestBody CreateBeneficiaryInternalDto dto);
}
