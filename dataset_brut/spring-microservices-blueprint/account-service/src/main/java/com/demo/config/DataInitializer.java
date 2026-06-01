package com.demo.config;

import com.demo.enums.ERole;
import com.demo.model.Role;
import com.demo.repo.RoleRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Configuration
public class DataInitializer {

    @Bean
    public ApplicationRunner seedRoles(RoleRepository roleRepository) {
        return args -> {
            if (roleRepository.findByName(ERole.ROLE_USER).isEmpty()) {
                roleRepository.save(new Role(null, ERole.ROLE_USER));
            }
            if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
                roleRepository.save(new Role(null, ERole.ROLE_ADMIN));
            }
        };
    }
}
