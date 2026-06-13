package com.donatech.order.client;

import com.donatech.order.controller.response.UserResponseDto;
import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {

    @Override
    public UserResponseDto getUserByEmail(String email) {
        return null;
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        return null;
    }

    @Override
    public com.donatech.order.dto.ContactDto getContactById(Long id) {
        return null;
    }
}
