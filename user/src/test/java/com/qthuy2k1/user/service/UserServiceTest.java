package com.qthuy2k1.user.service;

import com.qthuy2k1.user.dto.UserRequest;
import com.qthuy2k1.user.dto.UserResponse;
import com.qthuy2k1.user.exception.UserAlreadyExistsException;
import com.qthuy2k1.user.exception.UserNotFoundException;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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
    void willThrowWhenEmailIsTaken() {
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

    @Test
    void willGetAllUsers() {
        // when
        List<UserResponse> users = underTest.getAllUsers();

        // then
        verify(userRepository).findAll();
    }

    @Test
    void willDeleteUser() throws UserNotFoundException {
        // given
        UserModel user = new UserModel(1, "John Doe", "doe@gmail.com", "123123", Role.USER);

        when(userRepository.findById(user.getId().toString())).thenReturn(Optional.of(user));

        // when
        underTest.deleteUserById(user.getId().toString());

        // then
        verify(userRepository).delete(user);
    }

    @Test
    void willThrowException_IfUserNotFound_WhenDelete() {
        // given
        String id = "1";

        given(userRepository.findById(id)).willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() ->
                underTest.deleteUserById(id)).
                hasMessageContaining("user not found with ID: " + id);

        verify(userRepository, never()).delete(any());
    }
}