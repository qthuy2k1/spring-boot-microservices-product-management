package com.qthuy2k1.user.repository;

import com.qthuy2k1.user.model.UserModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

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
        UserModel user = new UserModel("John Doe", "doe@gmail.com", "123123");
        underTest.save(user);

        // when
        boolean userFindByEmail = underTest.existsByEmail(email);

        // then
        assertThat(userFindByEmail).isTrue();
    }
}