package com.qthuy2k1.userservice.config;

import com.qthuy2k1.userservice.enums.RoleEnum;
import com.qthuy2k1.userservice.model.UserModel;
import com.qthuy2k1.userservice.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        return args -> {
            if (userRepository.findByEmail("admin@gmail.com").isEmpty()) {
                var roles = new HashSet<String>();
                roles.add(RoleEnum.ADMIN.name());

                UserModel user = UserModel.builder()
                        .email("admin@gmail.com")
                        .name("admin")
                        .password(passwordEncoder.encode("admin"))
//                        .role(roles)
                        .build();

                userRepository.save(user);
                log.warn("admin user has been created with default password: admin, please change it");
            }
        };
    }
}
