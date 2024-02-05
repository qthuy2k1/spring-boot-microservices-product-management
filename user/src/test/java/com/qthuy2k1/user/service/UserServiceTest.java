package com.qthuy2k1.user.service;

import com.qthuy2k1.user.dto.UserRequest;
import com.qthuy2k1.user.exception.UserAlreadyExistsException;
import com.qthuy2k1.user.model.Role;
import com.qthuy2k1.user.model.UserModel;
import com.qthuy2k1.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCrypt;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    private UserService underTest;

    @BeforeEach
    void setUp() {
        underTest = new UserService(userRepository);
    }

    @Test
    void shouldCreateUser() throws UserAlreadyExistsException {
        // given
        UserRequest user = new UserRequest("John Doe", "doe@gmail.com", "123123");

        // when
        underTest.createUser(user);

        // then
        ArgumentCaptor<UserModel> userArgumentCaptor = ArgumentCaptor.forClass(UserModel.class);
        verify(userRepository).save(userArgumentCaptor.capture());

        UserModel capturedUser = userArgumentCaptor.getValue();

        assertThat(capturedUser.getName()).isEqualTo(user.getName());
        assertThat(capturedUser.getEmail()).isEqualTo(user.getEmail());
        assertThat(BCrypt.checkpw(user.getPassword(), capturedUser.getPassword())).isTrue();
        assertThat(capturedUser.getRole()).isEqualTo(Role.USER);

    }

    @Test()
    void willThrowWhenEmailIsTaken() throws UserAlreadyExistsException {
        // given
        UserRequest user = new UserRequest("John Doe", "doe@gmail.com", "123123");

        // when
        given(userRepository.existsByEmail(user.getEmail())).willReturn(true);

        // then
        assertThatThrownBy(() ->
                underTest.createUser(user)).
                hasMessageContaining("user already exists with email: " + user.getEmail());

        verify(userRepository, never()).save(any());
    }
}