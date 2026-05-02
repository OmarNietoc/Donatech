package com.donatech.auth.security.services;

import com.donatech.auth.client.UserServiceClient;
import com.donatech.auth.dto.UserCredentialsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserServiceClient userServiceClient;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            UserCredentialsDto creds = userServiceClient.getCredentialsByEmail(email);
            if (creds == null) {
                throw new UsernameNotFoundException("User not found with email: " + email);
            }
            return new UserDetailsImpl(creds.getId(), creds.getEmail(), creds.getPassword(), creds.getRoleName());
        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found with email: " + email, e);
        }
    }
}
