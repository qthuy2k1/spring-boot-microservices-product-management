package com.qthuy2k1.userservice.repository;

import com.qthuy2k1.userservice.model.UserModel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class UserRepositoryTest {
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );
    @Autowired
    private UserRepository underTest;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        underTest.deleteAll();
    }

    @Test
    void shouldFindByEmail() {
        // given
        String email = "doe@gmail.com";
        UserModel user = UserModel.builder()
                .name("John Doe")
                .email("doe@gmail.com")
                .password("123123")
                .build();
        underTest.save(user);

        // when
        Optional<UserModel> userFindByEmail = underTest.findByEmail(email);

        // then
        assertThat(userFindByEmail).isNotNull();
        assertThat(userFindByEmail.isPresent()).isTrue();
        assertThat(userFindByEmail.get()).isEqualTo(user);
    }

    @Test
    void shouldExistsByEmail() {
        // given
        String email = "doe@gmail.com";
        UserModel user = UserModel.builder()
                .name("John Doe")
                .email("doe@gmail.com")
                .password("123123")
                .build();
        underTest.save(user);

        // when
        boolean userExistsByEmail = underTest.existsByEmail(email);

        // then
        assertThat(userExistsByEmail).isTrue();
    }
}