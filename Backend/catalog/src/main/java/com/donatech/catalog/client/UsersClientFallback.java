package com.donatech.catalog.client;

import org.springframework.stereotype.Component;

@Component
public class UsersClientFallback implements UsersClient {

    @Override
    public UserStatusDto getUserById(Long id) {
        return null;
    }
}
