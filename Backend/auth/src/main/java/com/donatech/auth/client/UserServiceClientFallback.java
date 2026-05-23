package com.donatech.auth.client;

import com.donatech.auth.dto.CreateBeneficiaryInternalDto;
import com.donatech.auth.dto.CreateCompanyInternalDto;
import com.donatech.auth.dto.RegisterRequest;
import com.donatech.auth.dto.UserCredentialsDto;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public UserCredentialsDto getCredentialsByEmail(String email) {
        throw new RuntimeException("users-service no disponible — getCredentialsByEmail fallback");
    }

    @Override
    public UserCredentialsDto createUser(RegisterRequest request) {
        throw new RuntimeException("users-service no disponible — createUser fallback");
    }

    @Override
    public Map<String, Long> createBeneficiary(CreateBeneficiaryInternalDto dto) {
        throw new RuntimeException("users-service no disponible — createBeneficiary fallback");
    }

    @Override
    public Map<String, Long> createCompanyDetails(CreateCompanyInternalDto dto) {
        throw new RuntimeException("users-service no disponible — createCompanyDetails fallback");
    }
}
