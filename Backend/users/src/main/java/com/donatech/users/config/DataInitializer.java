package com.donatech.users.config;

import com.donatech.users.model.Role;
import com.donatech.users.model.User;
import com.donatech.users.repository.RoleRepository;
import com.donatech.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail("admin@donatech.cl")) return;

        roleRepository.findByName("ROLE_ADMIN").ifPresent(adminRole -> {
            User admin = new User(
                    "Administrador",
                    "admin@donatech.cl",
                    passwordEncoder.encode("Admin@2024"),
                    adminRole,
                    1);
            admin.setApellido("Donatech");
            userRepository.save(admin);
        });
    }
}
