package com.qthuy2k1.userservice.repository;

import com.qthuy2k1.userservice.model.UserModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository underTest;

    @AfterEach
    void tearDown() {
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