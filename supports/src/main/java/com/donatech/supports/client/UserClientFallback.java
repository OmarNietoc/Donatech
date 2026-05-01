package com.donatech.supports.client;

import com.donatech.supports.dto.UsuarioDTO;
import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {

    @Override
    public UsuarioDTO getUserById(Long id) {
        return null;
    }
}
